package lbycpds.group2.logic;

import lbycpds.group2.database.QuestionHistoryStore;
import lbycpds.group2.database.SessionStore;
import lbycpds.group2.database.TopicStore;
import lbycpds.group2.model.Question;
import lbycpds.group2.model.Student;
import lbycpds.group2.model.Session;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MissedQuestionsStudySession extends StudySession {
    private final String targetCourse;
    private final List<Integer> missedQuestionIds;

    private int sessionScore = 0;
    private int sessionTotalQuestions = 0;
    private long turnStartTime;

    public MissedQuestionsStudySession(Student student,
                                       TopicStore topicStore,
                                       SessionStore sessionStore,
                                       QuestionHistoryStore historyStore,
                                       String targetCourse) {
        super(student, topicStore, sessionStore, historyStore);
        if (student == null) throw new IllegalArgumentException("Missed review requires a student.");
        if (targetCourse == null || targetCourse.trim().isEmpty()) {
            throw new IllegalArgumentException("Missed review requires a target course.");
        }
        this.targetCourse = targetCourse;
        List<Integer> loaded = historyStore != null
                ? historyStore.loadMissed(student.getName(), targetCourse) : null;
        this.missedQuestionIds = loaded != null ? new ArrayList<>(loaded) : new ArrayList<>();
        this.turnStartTime = System.currentTimeMillis();
    }

    @Override
    public String nextCourse() {
        return this.targetCourse;
    }

    public List<Integer> getMissedQuestionIds() {
        return java.util.Collections.unmodifiableList(missedQuestionIds);
    }

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
            //Remove it from the missed list
            if (historyStore != null) {
                historyStore.removeMissed(student.getName(), targetCourse, questionId);
            }
        } else {
            //Keep it in missed list if still incorrect
            if (historyStore != null) {
                historyStore.addMissed(student.getName(), targetCourse, questionId);
            }
        }

        if (historyStore != null) {
            historyStore.addRecent(student.getName(), targetCourse, questionId);
        }
    }

    @Override
    public void finishTurn(String course) {
        if (!this.targetCourse.equalsIgnoreCase(course) || sessionTotalQuestions == 0) {
            return;
        }

        long elapsedTimeMs = System.currentTimeMillis() - turnStartTime;
        int timeSpentSeconds = (int) (elapsedTimeMs / 1000);
        int percent = Math.round(100f * sessionScore / sessionTotalQuestions);

        //Save session history record
        if (sessionStore != null) {
            Session sessionRecord = new Session(targetCourse, LocalDate.now(), percent, timeSpentSeconds);
            sessionStore.saveSession(student.getName(), sessionRecord);
        }

        //Reset stats
        this.sessionScore = 0;
        this.sessionTotalQuestions = 0;
        this.turnStartTime = System.currentTimeMillis();
    }
}

