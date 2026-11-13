package lbycpds.group2.test;
import lbycpds.group2.database.SessionStore;
import lbycpds.group2.model.Session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MockSessionStore implements SessionStore {
    private final Map<String, List<Session>> sessionMap = new HashMap<>();

    @Override
    public void saveSession(String studentName, Session session) {
        String key = studentName + "_" + session.getCourse();
        sessionMap.putIfAbsent(key, new ArrayList<>());
        sessionMap.get(key).add(session);
    }

    @Override
    public List<Session> loadSessions(String studentName, String course) {
        String key = studentName + "_" + course;
        return sessionMap.getOrDefault(key, new ArrayList<>());
    }
}
