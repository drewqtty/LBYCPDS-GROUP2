package lbycpds.group2.database;

import lbycpds.group2.model.Session;
import java.util.List;

public interface SessionStore {
    void saveSession(String studentName, Session session);

    List<Session> loadSessions(String studentName, String course);
}
