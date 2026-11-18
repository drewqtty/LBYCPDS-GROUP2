package lbycpds.group2.logic;

import lbycpds.group2.database.CramStore;
import lbycpds.group2.database.QuestionHistoryStore;
import lbycpds.group2.database.SessionStore;
import lbycpds.group2.database.TopicStore;
import lbycpds.group2.model.Question;
import lbycpds.group2.model.Session;
import lbycpds.group2.model.Student;

import java.time.LocalDate;

public class CramStudySession extends StudySession {
    public static final int MAX_LEVEL = 3;

    private final String course;
    private final CramStore cramStore;
    private int level;
    private int sessionScore = 0;
    private int sessionTotalQuestions = 0;
    private long turnStartTime;
    private String lastSummary = "";

    public CramStudySession(Student student,
                            TopicStore topicStore,
                            SessionStore sessionStore,
                            QuestionHistoryStore historyStore,
                            CramStore cramStore,
                            String course) {
        super(student, topicStore, sessionStore, historyStore);
        if (student == null) throw new IllegalArgumentException("Cram mode requires a student.");
        if (course == null || course.trim().isEmpty()) {
            throw new IllegalArgumentException("Cram mode requires a course.");
        }
        this.course = course;
        this.cramStore = cramStore;
        int saved = cramStore != null ? cramStore.loadLevel(student.getName(), course) : 1;
        this.level = Math.max(1, Math.min(MAX_LEVEL, saved));
        this.turnStartTime = System.currentTimeMillis();
    }

    @Override
    public String nextCourse() {
        return course;
    }


    public int getLevel() {
        return level;
    }

    public String getLastSummary() {
        return lastSummary;
    }

    @Override
    public void recordAnswer(Question question, String studentAnswer) {
        if (question == null) return;

        boolean isCorrect = question.checkAnswer(studentAnswer);
        int questionId = question.getId();

        if (sessionTotalQuestions == 0) {
            turnStartTime = System.currentTimeMillis();
        }
        this.sessionTotalQuestions++;

        if (isCorrect) {
            this.sessionScore++;
            if (historyStore != null) {
                historyStore.removeMissed(student.getName(), course, questionId);
            }
        } else if (historyStore != null) {
            historyStore.addMissed(student.getName(), course, questionId);
        }

        if (historyStore != null) {
            historyStore.addRecent(student.getName(), course, questionId);
        }
    }

    @Override
    public void finishTurn(String finishedCourse) {
        if (!course.equalsIgnoreCase(finishedCourse) || sessionTotalQuestions == 0) return;

        int timeSpentSeconds = (int) ((System.currentTimeMillis() - turnStartTime) / 1000);
        int percent = Math.round(100f * sessionScore / sessionTotalQuestions);
        int playedLevel = level;
        boolean passed = percent >= StudyFlow.CRAM_PASS_PERCENT;

        if (passed && level < MAX_LEVEL) {
            level++;
        }

        if (cramStore != null) {
            cramStore.saveResult(student.getName(), course, level, percent);
        }
        if (sessionStore != null) {
            sessionStore.saveSession(student.getName(),
                    new Session(course, LocalDate.now(), percent, timeSpentSeconds));
        }

        if (passed && playedLevel < MAX_LEVEL) {
            lastSummary = "Level " + playedLevel + " cleared with " + percent + "%! "
                    + "Level " + level + " of " + course + " is next.";
        } else if (passed) {
            lastSummary = "Level " + playedLevel + " cleared with " + percent + "%! "
                    + "You finished every cram level of " + course + ". Keep practicing to stay sharp.";
        } else {
            lastSummary = "You scored " + percent + "%. You need " + StudyFlow.CRAM_PASS_PERCENT
                    + "% to clear level " + playedLevel + ", so you will take it again.";
        }

        this.sessionScore = 0;
        this.sessionTotalQuestions = 0;
        this.turnStartTime = System.currentTimeMillis();
    }
}