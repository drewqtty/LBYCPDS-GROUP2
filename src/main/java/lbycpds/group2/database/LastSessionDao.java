package lbycpds.group2.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class LastSessionDao extends BaseDao implements LastSessionStore {

    public LastSessionDao(Connection connection) {
        super(connection);
        createTable();
    }

    private void createTable() {
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS last_session ("
                    + "student_name TEXT PRIMARY KEY, "
                    + "course TEXT NOT NULL, "
                    + "mode TEXT NOT NULL, "
                    + "topic TEXT, "
                    + "updated_at TIMESTAMPTZ NOT NULL DEFAULT now())");
        } catch (SQLException e) {
            throw new IllegalStateException("Could not create the last_session table: " + e.getMessage());
        }
    }

    public void save(String studentName, String course, String mode, String topic) {
        update("INSERT INTO last_session (student_name, course, mode, topic, updated_at) "
                        + "VALUES (?, ?, ?, ?, now()) "
                        + "ON CONFLICT (student_name) DO UPDATE SET "
                        + "course = EXCLUDED.course, mode = EXCLUDED.mode, "
                        + "topic = EXCLUDED.topic, updated_at = now()",
                studentName, course, mode, topic);
    }

    public LastSession load(String studentName) {
        String sql = "SELECT course, mode, topic FROM last_session WHERE student_name = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, studentName);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return new LastSession(result.getString("course"),
                            result.getString("mode"), result.getString("topic"));
                }
                return null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not load the last session: " + e.getMessage());
        }
    }
}