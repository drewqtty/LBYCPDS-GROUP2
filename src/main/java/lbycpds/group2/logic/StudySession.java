package lbycpds.group2.logic;

import lbycpds.group2.database.QuestionHistoryStore;
import lbycpds.group2.database.SessionStore;
import lbycpds.group2.database.TopicStore;
import lbycpds.group2.model.Student;

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
}