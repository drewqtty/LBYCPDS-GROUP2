package lbycpds.group2.test;

import lbycpds.group2.database.DatabaseConnection;
import lbycpds.group2.database.QuestionHistoryDao;
import lbycpds.group2.database.SessionDao;
import lbycpds.group2.database.StudentDao;
import lbycpds.group2.database.TopicDao;
import lbycpds.group2.model.Session;
import lbycpds.group2.model.Student;
import lbycpds.group2.model.Topic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class DaoTest {
    private static final String TEST_NAME = "DaoTest Student";
    private static final String CIRCUITS = "Fundamentals of Electrical Circuits";
    private static final String DSA = "Data Structures and Algorithms";

    public static void main(String[] args) {
        DatabaseConnection database = new DatabaseConnection();
        try (Connection connection = database.open()) {
            testStudents(new StudentDao(connection));
            testTopics(new TopicDao(connection));
            testSessions(new SessionDao(connection));
            testHistory(new QuestionHistoryDao(connection));
            cleanUp(connection);
            System.out.println("Test rows removed");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void testStudents(StudentDao dao) {
        Student student = new Student(TEST_NAME);
        student.addCourse(CIRCUITS);
        student.addCourse(DSA);
        dao.saveStudent(student);
        dao.saveStudent(student);
        Student loaded = dao.loadStudent(TEST_NAME);
        check("student loads with both courses", loaded != null && loaded.getCourses().size() == 2);
        check("unknown student returns null", dao.loadStudent("Nobody Here") == null);
    }

    private static void testTopics(TopicDao dao) {
        Topic resistance = new Topic("Resistance", CIRCUITS);
        resistance.setConfidence(40);
        resistance.setUnlocked(true);
        Topic series = new Topic("Series Circuits", CIRCUITS);
        series.setConfidence(20);
        dao.saveTopic(TEST_NAME, resistance);
        dao.saveTopic(TEST_NAME, series);
        check("two topics saved", dao.loadTopics(TEST_NAME, CIRCUITS).size() == 2);

        resistance.setLevel(2);
        resistance.setLatestScore(3);
        resistance.setBestScore(3);
        dao.saveTopic(TEST_NAME, resistance);
        List<Topic> topics = dao.loadTopics(TEST_NAME, CIRCUITS);
        check("saving again updates and does not duplicate", topics.size() == 2);
        Topic found = null;
        for (int i = 0; i < topics.size(); i++) {
            if (topics.get(i).getName().equals("Resistance")) {
                found = topics.get(i);
            }
        }
        check("updated values are loaded",
                found != null && found.getLevel() == 2 && found.getBestScore() == 3
                        && found.getConfidence() == 40 && found.isUnlocked());
        check("other course has no topics", dao.loadTopics(TEST_NAME, DSA).isEmpty());

        dao.saveLastTopic(TEST_NAME, CIRCUITS, "Resistance");
        check("last topic is saved", "Resistance".equals(dao.loadLastTopic(TEST_NAME, CIRCUITS)));
        check("last topic is empty for an untouched course", dao.loadLastTopic(TEST_NAME, DSA) == null);
    }

    private static void testSessions(SessionDao dao) {
        dao.saveSession(TEST_NAME, new Session(CIRCUITS, LocalDate.of(2026, 11, 10), 3, 120));
        dao.saveSession(TEST_NAME, new Session(CIRCUITS, LocalDate.of(2026, 11, 11), 2, 90));
        List<Session> sessions = dao.loadSessions(TEST_NAME, CIRCUITS);
        check("two sessions saved", sessions.size() == 2);
        check("sessions come back in date order with their values",
                sessions.size() == 2
                        && sessions.get(0).getDate().equals(LocalDate.of(2026, 11, 10))
                        && sessions.get(0).getScore() == 3
                        && sessions.get(1).getTimeSpentSeconds() == 90);
    }

    private static void testHistory(QuestionHistoryDao dao) {
        dao.addMissed(TEST_NAME, CIRCUITS, 111);
        dao.addMissed(TEST_NAME, CIRCUITS, 112);
        dao.addMissed(TEST_NAME, CIRCUITS, 111);
        check("missed question is not duplicated", dao.loadMissed(TEST_NAME, CIRCUITS).size() == 2);
        dao.removeMissed(TEST_NAME, CIRCUITS, 111);
        List<Integer> missed = dao.loadMissed(TEST_NAME, CIRCUITS);
        check("missed question is removed", missed.size() == 1 && missed.get(0) == 112);

        for (int id = 201; id <= 212; id++) {
            dao.addRecent(TEST_NAME, CIRCUITS, id);
        }
        List<Integer> recent = dao.loadRecent(TEST_NAME, CIRCUITS);
        check("only the 10 most recent questions are returned", recent.size() == 10);
        check("the newest question comes first", recent.get(0) == 212);
    }

    private static void check(String label, boolean ok) {
        System.out.println((ok ? "PASS: " : "FAIL: ") + label);
    }

    private static void cleanUp(Connection connection) throws SQLException {
        String[] tables = {"missed_questions", "recent_questions", "sessions", "topic_progress", "student_courses"};
        for (int i = 0; i < tables.length; i++) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM " + tables[i] + " WHERE student_name = ?")) {
                statement.setString(1, TEST_NAME);
                statement.executeUpdate();
            }
        }
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM students WHERE name = ?")) {
            statement.setString(1, TEST_NAME);
            statement.executeUpdate();
        }
    }
}