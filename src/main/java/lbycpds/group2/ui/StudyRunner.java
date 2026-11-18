package lbycpds.group2.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import lbycpds.group2.StudySessionController;
import lbycpds.group2.database.LocalHistoryStore;
import lbycpds.group2.database.LocalSessionStore;
import lbycpds.group2.database.LocalTopicStore;
import lbycpds.group2.database.Stores;
import lbycpds.group2.logic.AdaptiveDifficulty;
import lbycpds.group2.logic.MissedQuestionsStudySession;
import lbycpds.group2.logic.QuestionBankFiles;
import lbycpds.group2.logic.StudyFlow;
import lbycpds.group2.logic.StudyMode;
import lbycpds.group2.logic.StudySession;
import lbycpds.group2.logic.TextFileQuestionBank;
import lbycpds.group2.model.Question;
import lbycpds.group2.model.Topic;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class StudyRunner {

    private static final int QUESTIONS_PER_ROUND = 10;
    private static final String[] BANK_FILES = {"/questions/circuits.txt", "/questions/dsa.txt"};

    public static void start() {
        String student = AppState.student.getName();
        AppState.topicStore = new LocalTopicStore(AppState.topics,
                Stores.isConnected() ? Stores.topicStore() : null);
        AppState.sessionStore = new LocalSessionStore(
                Stores.isConnected() ? Stores.sessionStore() : null);
        AppState.historyStore = new LocalHistoryStore(
                Stores.isConnected() ? Stores.historyStore() : null);

        StudySessionController controller = controller();
        if ("CRAM".equals(AppState.mode)) {
            AppState.session = controller.createSession(StudyMode.CRAM, AppState.student,
                    null, null, null, AppState.graphs);
        } else {
            AppState.session = controller.createSession(StudyMode.NORMAL, AppState.student,
                    AppState.course, AppState.graphs.get(AppState.course), null, null);
        }
        nextRound();
    }

    public static void nextRound() {
        StudySession session = AppState.session;
        if (session == null) {
            SceneManager.show(new MainMenu().build());
            return;
        }

        String course = session.nextCourse();
        Topic topic = session.getTargetTopic();
        TextFileQuestionBank bank = bankFor(course);
        if (topic == null || bank == null) {
            tell("There is no topic to study for " + course + " yet.");
            SceneManager.show(new MainMenu().build());
            return;
        }

        List<Integer> recent = AppState.historyStore.loadRecent(AppState.student.getName(), course);
        List<Question> picked = StudyFlow.pickQuestions(bank.loadQuestions(), topic, recent, QUESTIONS_PER_ROUND);
        if (picked.isEmpty()) {
            tell("There are no questions for " + topic.getName() + " yet.");
            SceneManager.show(new MainMenu().build());
            return;
        }

        SceneManager.show(new QuizScreen(picked, session::recordAnswer, result -> {
            session.finishTurn(course);
            saveAllTopics();
            showReport();
        }).build());
    }

    // Quiz again on the questions the student missed before
    public static void reviewMissed() {
        StudySessionController controller = controller();
        String student = AppState.student.getName();

        List<Question> review = new ArrayList<>();
        Map<String, MissedQuestionsStudySession> sessions = new LinkedHashMap<>();
        for (String course : AppState.student.getCourses()) {
            TextFileQuestionBank bank = bankFor(course);
            if (bank == null) {
                continue;
            }
            MissedQuestionsStudySession missed = (MissedQuestionsStudySession) controller.createSession(
                    StudyMode.MISSED, AppState.student, course, null, null, null);
            review.addAll(StudyFlow.missedQuestions(bank.loadQuestions(), course, missed.getMissedQuestionIds()));
            sessions.put(course, missed);
        }

        if (review.isEmpty()) {
            tell("You have no missed questions to review right now.");
            return;
        }

        SceneManager.show(new QuizScreen(review,
                (question, answer) -> sessions.get(question.getCourse()).recordAnswer(question, answer),
                result -> {
                    for (Map.Entry<String, MissedQuestionsStudySession> entry : sessions.entrySet()) {
                        entry.getValue().finishTurn(entry.getKey());
                    }
                    showReport();
                }).build());
    }

    private static void showReport() {
        SceneManager.show(new ReportScreen(AppState.topics, AppState.sessionStore.all()).build());
    }

    private static void saveAllTopics() {
        for (Topic topic : AppState.topics) {
            AppState.topicStore.saveTopic(AppState.student.getName(), topic);
        }
    }

    private static StudySessionController controller() {
        return new StudySessionController(AppState.topicStore, AppState.sessionStore,
                AppState.historyStore, new AdaptiveDifficulty());
    }

    private static TextFileQuestionBank bankFor(String course) {
        for (String file : BANK_FILES) {
            TextFileQuestionBank bank = QuestionBankFiles.open(file);
            if (bank.getCourse().equals(course)) {
                return bank;
            }
        }
        return null;
    }

    private static void tell(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
