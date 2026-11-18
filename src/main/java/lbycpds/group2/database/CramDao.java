package lbycpds.group2.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CramDao extends BaseDao implements CramStore {

    public CramDao(Connection connection) {
        super(connection);
        createTable();
    }

    private void createTable() {
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS cram_progress ("
                    + "student_name TEXT NOT NULL, "
                    + "course TEXT NOT NULL, "
                    + "level INTEGER NOT NULL DEFAULT 1, "
                    + "best_score INTEGER NOT NULL DEFAULT 0, "
                    + "latest_score INTEGER NOT NULL DEFAULT 0, "
                    + "updated_at TIMESTAMPTZ NOT NULL DEFAULT now(), "
                    + "PRIMARY KEY (student_name, course))");
        } catch (SQLException e) {
            throw new IllegalStateException("Could not create the cram_progress table: " + e.getMessage());
        }
    }

    public int loadLevel(String studentName, String course) {
        String sql = "SELECT level FROM cram_progress WHERE student_name = ? AND course = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, studentName);
            statement.setString(2, course);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getInt("level") : 1;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not load the cram level: " + e.getMessage());
        }
    }

    public void saveResult(String studentName, String course, int level, int percent) {
        update("INSERT INTO cram_progress (student_name, course, level, best_score, latest_score, updated_at) "
                        + "VALUES (?, ?, ?, ?, ?, now()) "
                        + "ON CONFLICT (student_name, course) DO UPDATE SET "
                        + "level = EXCLUDED.level, latest_score = EXCLUDED.latest_score, "
                        + "best_score = GREATEST(cram_progress.best_score, EXCLUDED.best_score), "
                        + "updated_at = now()",
                studentName, course, level, percent, percent);
    }
}