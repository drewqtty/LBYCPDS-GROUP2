package lbycpds.group2.logic;

import lbycpds.group2.database.QuestionHistoryStore;
import lbycpds.group2.database.SessionStore;
import lbycpds.group2.database.TopicStore;
import lbycpds.group2.model.Question;
import lbycpds.group2.model.Student;
import lbycpds.group2.model.Topic;

public abstract class StudySession {
    protected Student student;
    protected TopicStore topicStore;
    protected SessionStore sessionStore;
    protected QuestionHistoryStore historyStore;

    public StudySession(Student student, TopicStore topicStore,
                        SessionStore sessionStore, QuestionHistoryStore historyStore) {
        this.student = student;
        this.topicStore = topicStore;
        this.sessionStore = sessionStore;
        this.historyStore = historyStore;
    }

    public abstract String nextCourse();

    public abstract void finishTurn(String course);

    public abstract void recordAnswer(Question question, String studentAnswer);

    // The topic being studied this turn
    public Topic getTargetTopic() {
        return null;
    }
}