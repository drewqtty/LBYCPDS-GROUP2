package lbycpds.group2.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class QuestionHistoryDao extends BaseDao implements QuestionHistoryStore {
    private static final int RECENT_LIMIT = 10;

    public QuestionHistoryDao(Connection connection) {
        super(connection);
    }

    public void addMissed(String studentName, String course, int questionId) {
        ensureCourse(studentName, course);
        update("INSERT INTO missed_questions (student_name, course, question_id) VALUES (?, ?, ?) "
                        + "ON CONFLICT (student_name, course, question_id) DO UPDATE SET missed_at = now()",
                studentName, course, questionId);
    }

    public void removeMissed(String studentName, String course, int questionId) {
        update("DELETE FROM missed_questions WHERE student_name = ? AND course = ? AND question_id = ?",
                studentName, course, questionId);
    }

    public List<Integer> loadMissed(String studentName, String course) {
        return loadIds("SELECT question_id FROM missed_questions "
                        + "WHERE student_name = ? AND course = ? ORDER BY missed_at",
                studentName, course);
    }

    public void addRecent(String studentName, String course, int questionId) {
        ensureCourse(studentName, course);
        update("INSERT INTO recent_questions (student_name, course, question_id) VALUES (?, ?, ?) "
                        + "ON CONFLICT (student_name, course, question_id) DO UPDATE SET answered_at = now()",
                studentName, course, questionId);
    }

    public List<Integer> loadRecent(String studentName, String course) {
        return loadIds("SELECT question_id FROM recent_questions "
                        + "WHERE student_name = ? AND course = ? ORDER BY answered_at DESC LIMIT " + RECENT_LIMIT,
                studentName, course);
    }

    private List<Integer> loadIds(String sql, String studentName, String course) {
        List<Integer> ids = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, studentName);
            statement.setString(2, course);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    ids.add(result.getInt("question_id"));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not load question history: " + e.getMessage());
        }
        return ids;
    }
}