package lbycpds.group2.test;

import lbycpds.group2.StudySessionController;
import lbycpds.group2.database.LocalCramStore;
import lbycpds.group2.database.QuestionHistoryStore;
import lbycpds.group2.database.TopicStore;
import lbycpds.group2.logic.*;
import lbycpds.group2.model.*;
import lbycpds.group2.structures.Graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StudySessionFixesTest {

    private static int passed = 0;
    private static int failed = 0;


    private static void section(String title, String problem) {
        System.out.println();
        System.out.println("==================================================================");
        System.out.println(title);
        System.out.println("  Problem: " + problem);
        System.out.println("==================================================================");
    }

    private static void info(String text) {
        System.out.println("  . " + text);
    }

    private static void check(String what, Object expected, Object actual) {
        boolean ok = expected == null ? actual == null : expected.equals(actual);
        record(ok, what, "expected: " + expected, "actual:   " + actual);
    }

    private static void checkTrue(String what, boolean condition, String detail) {
        record(condition, what, "expected: condition to hold", "actual:   " + detail);
    }

    private static void expectThrows(String what, Class<? extends Throwable> type, Runnable action) {
        String actual;
        boolean ok;
        try {
            action.run();
            actual = "no exception thrown";
            ok = false;
        } catch (Throwable t) {
            actual = t.getClass().getSimpleName() + (t.getMessage() != null ? " (\"" + t.getMessage() + "\")" : "");
            ok = type.isInstance(t);
        }
        record(ok, what, "expected: " + type.getSimpleName(), "actual:   " + actual);
    }

    private static void record(boolean ok, String what, String expectedLine, String actualLine) {
        if (ok) {
            passed++;
        } else {
            failed++;
        }
        System.out.println("  [" + (ok ? "PASS" : "FAIL") + "] " + what);
        System.out.println("         " + expectedLine);
        System.out.println("         " + actualLine);
    }

    private static class RecordingTopicStore extends MockTopicStore {
        final List<String> saved = new ArrayList<>();

        @Override
        public void saveTopic(String studentName, Topic topic) {
            saved.add(topic.getName() + "(latest=" + topic.getLatestScore() + ", level=" + topic.getLevel() + ")");
            super.saveTopic(studentName, topic);
        }
    }

    private static Topic topic(String name, String course, boolean unlocked, int confidence, int latest) {
        Topic t = new Topic(name, course);
        t.setUnlocked(unlocked);
        t.setConfidence(confidence);
        t.setLatestScore(latest);
        return t;
    }

    private static final Question Q = new IdentificationQuestion(1, "C", "A", 1, "Capital of France?", "Paris");

    private static void answerCorrectly(StudySession session, int times) {
        for (int i = 0; i < times; i++) {
            session.recordAnswer(Q, "Paris");
        }
    }

    private static Student student() {
        Student s = new Student("Tester");
        s.addCourse("C");
        return s;
    }


    public static void main(String[] args) throws Exception {
        System.out.println("STUDY SESSION FIXES TEST");
        System.out.println("Each section covers one change to StudySessionController or a session class.");

        fix1_finishTurnSavesTheTopicThatWasStudied();
        fix2_lockedTopicIsNeverHandedOut();
        fix3_controllerGuards();
        fix4_cramFinishTurnChecksCourse();
        fix5_missedSessionHardening();
        fix6_timeTrackingStartsAtFirstAnswer();

        System.out.println();
        System.out.println("==================================================================");
        System.out.println("SUMMARY: " + passed + " passed, " + failed + " failed, " + (passed + failed) + " checks");
        System.out.println("==================================================================");
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void fix1_finishTurnSavesTheTopicThatWasStudied() {
        section("FIX 1  SingleCourseStudySession: finishTurn updates and saves the topic that was studied",
                "finishTurn used to call getTargetTopic() again after the score changed, so the min-heap could "
                        + "pick a different topic and the wrong one was saved.");

        // Case 1a: the score of the studied topic jumps, which reorders the heap.
        RecordingTopicStore store = new RecordingTopicStore();
        Topic a = topic("A", "C", true, 1, 0);
        Topic b = topic("B", "C", true, 1, 50);
        store.setTopicsForCourse("Tester", "C", Arrays.asList(a, b));
        StudySessionController controller = new StudySessionController(
                store, new MockSessionStore(), new MockQuestionHistoryStore(), new AdaptiveDifficulty());
        SingleCourseStudySession session = (SingleCourseStudySession)
                controller.createSession(StudyMode.NORMAL, student(), "C", new Graph(), null, null);

        System.out.println();
        System.out.println(" Case 1a: studied topic's score rises, changing the heap order");
        info("A: confidence 1, latest 0   |   B: confidence 1, latest 50   (A is weaker, so A is quizzed)");
        Topic quizzed = session.getTargetTopic();
        check("topic chosen for the quiz", "A", quizzed.getName());
        answerCorrectly(session, 10);
        info("answered 10/10 correctly, calling finishTurn");
        session.finishTurn("C");
        info("topics saved to the store: " + store.saved);
        check("exactly one topic was saved", 1, store.saved.size());
        check("the saved topic is the one that was studied", "A(latest=100, level=2)", store.saved.get(0));
        check("A's latest score was updated", 100, a.getLatestScore());
        check("B was left untouched", 50, b.getLatestScore());
        check("next target rotates to B now that A is strong", "B", session.getTargetTopic().getName());

        // Case 1b: confidence is edited after the quiz began (ConfidenceScreen), which also reorders the heap.
        RecordingTopicStore store2 = new RecordingTopicStore();
        Topic a2 = topic("A", "C", true, 1, 0);
        Topic b2 = topic("B", "C", true, 2, 0);
        store2.setTopicsForCourse("Tester", "C", Arrays.asList(a2, b2));
        StudySessionController controller2 = new StudySessionController(
                store2, new MockSessionStore(), new MockQuestionHistoryStore(), new AdaptiveDifficulty());
        SingleCourseStudySession session2 = (SingleCourseStudySession)
                controller2.createSession(StudyMode.NORMAL, student(), "C", new Graph(), null, null);

        System.out.println();
        System.out.println(" Case 1b: confidence of the quizzed topic changes mid-turn");
        info("A: confidence 1   |   B: confidence 2   (A is quizzed)");
        check("topic chosen for the quiz", "A", session2.getTargetTopic().getName());
        a2.setConfidence(9);
        info("A's confidence is changed to 9 while the quiz is running (B is now the weakest)");
        answerCorrectly(session2, 10);
        session2.finishTurn("C");
        info("topics saved to the store: " + store2.saved);
        check("the saved topic is still A, the one that was quizzed", "A(latest=100, level=2)",
                store2.saved.isEmpty() ? "nothing saved" : store2.saved.get(0));
        check("B's score was not overwritten with A's result", 0, b2.getLatestScore());
    }

    private static void fix2_lockedTopicIsNeverHandedOut() {
        section("FIX 2  SingleCourseStudySession: no topic is returned when everything is locked",
                "It used to return the first topic even when it was locked, bypassing the prerequisite graph.");

        MockTopicStore store = new MockTopicStore();
        store.setTopicsForCourse("Tester", "ALL_LOCKED", Arrays.asList(
                topic("L1", "ALL_LOCKED", false, 0, 0), topic("L2", "ALL_LOCKED", false, 0, 0)));
        store.setTopicsForCourse("Tester", "MIXED", Arrays.asList(
                topic("Locked", "MIXED", false, 0, 0), topic("Open", "MIXED", true, 5, 0)));
        store.setTopicsForCourse("Tester", "EMPTY", new ArrayList<>());
        StudySessionController controller = new StudySessionController(
                store, new MockSessionStore(), new MockQuestionHistoryStore(), new AdaptiveDifficulty());

        info("course ALL_LOCKED has two locked topics");
        Topic none = controller.createSession(StudyMode.NORMAL, student(), "ALL_LOCKED", null, null, null)
                .getTargetTopic();
        check("target topic when everything is locked", null, none == null ? null : none.getName());

        info("course MIXED has one locked topic listed first and one unlocked topic");
        Topic open = controller.createSession(StudyMode.NORMAL, student(), "MIXED", null, null, null)
                .getTargetTopic();
        check("the unlocked topic is chosen (regression check)", "Open", open == null ? null : open.getName());

        info("course EMPTY has no topics at all");
        Topic empty = controller.createSession(StudyMode.NORMAL, student(), "EMPTY", null, null, null)
                .getTargetTopic();
        check("target topic when the course has no topics", null, empty == null ? null : empty.getName());
    }

    private static void fix3_controllerGuards() {
        section("FIX 3  StudySessionController: null mode and missing cram store",
                "A null mode used to throw NullPointerException from the switch, and a missing cram store "
                        + "silently produced a cram session that never saved its level.");

        MockTopicStore topics = new MockTopicStore();
        StudySessionController controller = new StudySessionController(
                topics, new MockSessionStore(), new MockQuestionHistoryStore(), new AdaptiveDifficulty());

        expectThrows("createSession(null mode) reports a clear error", IllegalArgumentException.class,
                () -> controller.createSession(null, student(), "C", null, null, null));

        expectThrows("createCramSession without setCramStore fails loudly", IllegalStateException.class,
                () -> controller.createCramSession(student(), "C"));
        expectThrows("createSession(CRAM) without setCramStore fails loudly", IllegalStateException.class,
                () -> controller.createSession(StudyMode.CRAM, student(), "C", null, null, null));

        controller.setCramStore(new LocalCramStore(null));
        info("setCramStore(new LocalCramStore(null)) was called");
        CramStudySession cram = controller.createCramSession(student(), "C");
        check("cram session is created once the store is set (starting level)", 1, cram.getLevel());

        info("regression: the existing blank-course checks still work");
        expectThrows("NORMAL with a blank course", IllegalArgumentException.class,
                () -> controller.createSession(StudyMode.NORMAL, student(), "  ", null, null, null));
        expectThrows("CRAM with a null course", IllegalArgumentException.class,
                () -> controller.createSession(StudyMode.CRAM, student(), null, null, null, null));
        expectThrows("MISSED with a blank course", IllegalArgumentException.class,
                () -> controller.createSession(StudyMode.MISSED, student(), "", null, null, null));
    }

    private static void fix4_cramFinishTurnChecksCourse() {
        section("FIX 4  CramStudySession: finishTurn only counts for its own course; student is required",
                "finishTurn ignored its course argument (the other sessions guard on it), and a null student "
                        + "crashed with a NullPointerException.");

        MockSessionStore sessions = new MockSessionStore();
        LocalCramStore cramStore = new LocalCramStore(null);
        StudySessionController controller = new StudySessionController(
                new MockTopicStore(), sessions, new MockQuestionHistoryStore(), new AdaptiveDifficulty());
        controller.setCramStore(cramStore);
        CramStudySession cram = controller.createCramSession(student(), "C");

        answerCorrectly(cram, 5);
        info("5/5 correct recorded in cram level " + cram.getLevel() + " of course C");

        cram.finishTurn("SOME_OTHER_COURSE");
        info("finishTurn(\"SOME_OTHER_COURSE\") was called");
        check("no session record was saved for the wrong course", 0, sessions.loadSessions("Tester", "C").size());
        check("level did not change", 1, cram.getLevel());
        check("no summary was produced", "", cram.getLastSummary());

        cram.finishTurn("c");
        info("finishTurn(\"c\") was called (same course, different case)");
        check("one session record was saved", 1, sessions.loadSessions("Tester", "C").size());
        check("level went up after passing", 2, cram.getLevel());
        check("level was persisted to the cram store", 2, cramStore.loadLevel("Tester", "C"));
        info("summary: " + cram.getLastSummary());
        checkTrue("summary mentions the cleared level", cram.getLastSummary().contains("Level 1 cleared"),
                cram.getLastSummary());

        expectThrows("constructing a cram session with a null student", IllegalArgumentException.class,
                () -> new CramStudySession(null, new MockTopicStore(), sessions,
                        new MockQuestionHistoryStore(), cramStore, "C"));
    }

    private static void fix5_missedSessionHardening() {
        section("FIX 5  MissedQuestionsStudySession: null-safe, read-only and validated",
                "A store returning null caused a later NullPointerException, callers could modify the internal "
                        + "list, and a blank course was accepted.");

        // A store whose loadMissed returns null
        QuestionHistoryStore nullStore = new QuestionHistoryStore() {
            public void addMissed(String s, String c, int id) { }
            public void removeMissed(String s, String c, int id) { }
            public List<Integer> loadMissed(String s, String c) { return null; }
            public void addRecent(String s, String c, int id) { }
            public List<Integer> loadRecent(String s, String c) { return new ArrayList<>(); }
        };
        MissedQuestionsStudySession fromNull =
                new MissedQuestionsStudySession(student(), new MockTopicStore(), new MockSessionStore(), nullStore, "C");
        info("history store returns null from loadMissed");
        check("missed id list is an empty list, not null", "[]", String.valueOf(fromNull.getMissedQuestionIds()));

        // Read-only getter + snapshot
        MockQuestionHistoryStore history = new MockQuestionHistoryStore();
        history.addMissed("Tester", "C", 7);
        history.addMissed("Tester", "C", 8);
        MissedQuestionsStudySession missed = new MissedQuestionsStudySession(
                student(), new MockTopicStore(), new MockSessionStore(), history, "C");
        check("session loaded the stored missed ids", "[7, 8]", String.valueOf(missed.getMissedQuestionIds()));
        expectThrows("caller cannot modify the list returned by getMissedQuestionIds",
                UnsupportedOperationException.class, () -> missed.getMissedQuestionIds().add(99));
        history.removeMissed("Tester", "C", 7);
        info("question 7 was then removed from the store after the session was created");
        check("the session keeps its own snapshot of the ids", "[7, 8]", String.valueOf(missed.getMissedQuestionIds()));

        expectThrows("blank course is rejected", IllegalArgumentException.class,
                () -> new MissedQuestionsStudySession(student(), new MockTopicStore(),
                        new MockSessionStore(), history, "   "));
        expectThrows("null course is rejected", IllegalArgumentException.class,
                () -> new MissedQuestionsStudySession(student(), new MockTopicStore(),
                        new MockSessionStore(), history, null));
    }

    private static void fix6_timeTrackingStartsAtFirstAnswer() throws InterruptedException {
        section("FIX 6  All three sessions: study time starts at the first answer",
                "The clock used to start when the session was created (or when the last turn ended), so time "
                        + "spent idle on a menu or report screen was counted as study time.");

        MockSessionStore sessions = new MockSessionStore();
        MockTopicStore topics = new MockTopicStore();
        topics.setTopicsForCourse("Tester", "C", Arrays.asList(topic("A", "C", true, 1, 0)));
        MockQuestionHistoryStore history = new MockQuestionHistoryStore();
        StudySessionController controller = new StudySessionController(
                topics, sessions, history, new AdaptiveDifficulty());
        controller.setCramStore(new LocalCramStore(null));

        SingleCourseStudySession single = (SingleCourseStudySession)
                controller.createSession(StudyMode.NORMAL, student(), "C", null, null, null);
        CramStudySession cram = controller.createCramSession(student(), "C");
        history.addMissed("Tester", "C", 1);
        MissedQuestionsStudySession missed = (MissedQuestionsStudySession)
                controller.createSession(StudyMode.MISSED, student(), "C", null, null, null);

        info("all three sessions were created, then the test waits 2.2 seconds doing nothing");
        long startedWaiting = System.currentTimeMillis();
        Thread.sleep(2200);
        double waited = (System.currentTimeMillis() - startedWaiting) / 1000.0;
        info("waited " + waited + " s, now each session gets one answer and finishes immediately");

        single.getTargetTopic();
        single.recordAnswer(Q, "Paris");
        single.finishTurn("C");
        cram.recordAnswer(Q, "Paris");
        cram.finishTurn("C");
        missed.recordAnswer(Q, "Paris");
        missed.finishTurn("C");

        List<Session> saved = sessions.loadSessions("Tester", "C");
        check("three session records were saved", 3, saved.size());
        String[] names = {"regular", "cram", "missed"};
        for (int i = 0; i < saved.size(); i++) {
            int seconds = saved.get(i).getTimeSpentSeconds();
            checkTrue(names[i] + " session did not count the idle wait as study time", seconds < 2,
                    seconds + " s recorded (idle wait was " + waited + " s)");
        }
    }
}
