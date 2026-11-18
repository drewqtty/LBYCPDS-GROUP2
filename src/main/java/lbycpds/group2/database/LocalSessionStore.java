package lbycpds.group2.database;

import lbycpds.group2.model.Session;

import java.util.ArrayList;
import java.util.List;


public class LocalSessionStore implements SessionStore {
    private final List<Session> sessions = new ArrayList<>();
    private final SessionStore saved;   // null when there is no database

    public LocalSessionStore(SessionStore saved) {
        this.saved = saved;
    }

    @Override
    public void saveSession(String studentName, Session session) {
        sessions.add(session);
        if (saved != null) {
            saved.saveSession(studentName, session);
        }
    }

    @Override
    public List<Session> loadSessions(String studentName, String course) {
        List<Session> found = new ArrayList<>();
        for (Session session : sessions) {
            if (session.getCourse().equals(course)) {
                found.add(session);
            }
        }
        return found;
    }

    // Every session done since the app was opened
    public List<Session> all() {
        return new ArrayList<>(sessions);
    }
}
