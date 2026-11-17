package lbycpds.group2.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class BaseDao {
    protected Connection connection;

    public BaseDao(Connection connection) {
        this.connection = connection;
    }

    protected int update(String sql, Object... values) {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) {
                statement.setObject(i + 1, values[i]);
            }
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Database error: " + e.getMessage());
        }
    }

    protected void ensureStudent(String studentName) {
        update("INSERT INTO students (name) VALUES (?) ON CONFLICT (name) DO NOTHING",
                studentName);
    }

    protected void ensureCourse(String studentName, String course) {
        ensureStudent(studentName);
        update("INSERT INTO student_courses (student_name, course) VALUES (?, ?) "
                        + "ON CONFLICT (student_name, course) DO NOTHING",
                studentName, course);
    }
}