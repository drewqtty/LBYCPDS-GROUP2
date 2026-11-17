package lbycpds.group2.database;

import lbycpds.group2.model.Session;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SessionDao extends BaseDao implements SessionStore {

    public SessionDao(Connection connection) {
        super(connection);
    }

    public void saveSession(String studentName, Session session) {
        ensureCourse(studentName, session.getCourse());
        update("INSERT INTO sessions (student_name, course, session_date, score, time_spent_seconds) "
                        + "VALUES (?, ?, ?, ?, ?)",
                studentName, session.getCourse(), session.getDate(),
                session.getScore(), session.getTimeSpentSeconds());
    }

    public List<Session> loadSessions(String studentName, String course) {
        List<Session> sessions = new ArrayList<>();
        String sql = "SELECT session_date, score, time_spent_seconds FROM sessions "
                + "WHERE student_name = ? AND course = ? ORDER BY session_date, id";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, studentName);
            statement.setString(2, course);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    sessions.add(new Session(course,
                            result.getDate("session_date").toLocalDate(),
                            result.getInt("score"),
                            result.getInt("time_spent_seconds")));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not load sessions: " + e.getMessage());
        }
        return sessions;
    }
}