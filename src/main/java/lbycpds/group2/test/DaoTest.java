package lbycpds.group2.test;

import lbycpds.group2.database.DatabaseConnection;
import lbycpds.group2.database.StudentDao;
import lbycpds.group2.model.Student;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DaoTest {
    private static final String TEST_NAME = "DaoTest Student";

    public static void main(String[] args) {
        DatabaseConnection database = new DatabaseConnection();
        try (Connection connection = database.open()) {
            StudentDao studentDao = new StudentDao(connection);

            Student student = new Student(TEST_NAME);
            student.addCourse("Fundamentals of Electrical Circuits");
            student.addCourse("Data Structures and Algorithms");
            studentDao.saveStudent(student);
            studentDao.saveStudent(student);

            Student loaded = studentDao.loadStudent(TEST_NAME);
            System.out.println("Loaded: " + loaded.getName() + " " + loaded.getCourses());
            System.out.println("Unknown student: " + studentDao.loadStudent("Nobody Here"));

            cleanUp(connection);
            System.out.println("Test rows removed");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void cleanUp(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM student_courses WHERE student_name = '" + TEST_NAME + "'");
            statement.executeUpdate("DELETE FROM students WHERE name = '" + TEST_NAME + "'");
        }
    }
}