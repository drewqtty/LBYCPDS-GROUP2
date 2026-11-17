package lbycpds.group2.database;

import lbycpds.group2.model.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StudentDao extends BaseDao implements StudentStore {

    public StudentDao(Connection connection) {
        super(connection);
    }

    public Student loadStudent(String name) {
        try {
            if (!studentExists(name)) {
                return null;
            }
            Student student = new Student(name);
            String sql = "SELECT course FROM student_courses WHERE student_name = ? ORDER BY course";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, name);
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        student.addCourse(result.getString("course"));
                    }
                }
            }
            return student;
        } catch (SQLException e) {
            throw new IllegalStateException("Could not load student " + name + ": " + e.getMessage());
        }
    }

    public void saveStudent(Student student) {
        ensureStudent(student.getName());
        for (int i = 0; i < student.getCourses().size(); i++) {
            ensureCourse(student.getName(), student.getCourses().get(i));
        }
    }

    private boolean studentExists(String name) throws SQLException {
        String sql = "SELECT 1 FROM students WHERE name = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }
}