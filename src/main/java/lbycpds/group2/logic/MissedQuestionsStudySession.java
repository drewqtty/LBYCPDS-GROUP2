package lbycpds.group2.logic;

import lbycpds.group2.database.QuestionHistoryStore;
import lbycpds.group2.database.SessionStore;
import lbycpds.group2.database.TopicStore;
import lbycpds.group2.model.Question;
import lbycpds.group2.model.Student;
import lbycpds.group2.model.Session;

import java.time.LocalDate;
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
        this.targetCourse = targetCourse;
        this.missedQuestionIds = historyStore != null ? historyStore.loadMissed(student.getName(), targetCourse) : List.of();
        this.turnStartTime = System.currentTimeMillis();
    }

    @Override
    public String nextCourse() {
        return this.targetCourse;
    }

    public List<Integer> getMissedQuestionIds() {
        return missedQuestionIds;
    }

    public void recordAnswer(Question question, String studentAnswer) {
        if (question == null) return;

        boolean isCorrect = question.checkAnswer(studentAnswer);
        int questionId = question.getId();

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

        //Save session history record
        if (sessionStore != null) {
            Session sessionRecord = new Session(targetCourse, LocalDate.now(), sessionScore, timeSpentSeconds);
            sessionStore.saveSession(student.getName(), sessionRecord);
        }

        //Reset stats
        this.sessionScore = 0;
        this.sessionTotalQuestions = 0;
        this.turnStartTime = System.currentTimeMillis();
    }
}

