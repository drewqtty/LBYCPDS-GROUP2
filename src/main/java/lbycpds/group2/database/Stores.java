package lbycpds.group2.database;

import java.sql.Connection;
import java.sql.SQLException;

public class Stores {
    private static Connection connection;
    private static StudentStore studentStore;
    private static TopicStore topicStore;
    private static SessionStore sessionStore;
    private static QuestionHistoryStore historyStore;

    public static boolean connect() {
        try {
            connection = new DatabaseConnection().open();
        } catch (SQLException | IllegalStateException e) {
            connection = null;
            System.out.println("Database offline, progress will not be saved: " + e.getMessage());
            return false;
        }
        studentStore = new StudentDao(connection);
        topicStore = new TopicDao(connection);
        sessionStore = new SessionDao(connection);
        historyStore = new QuestionHistoryDao(connection);
        return true;
    }

    public static boolean isConnected() {
        return connection != null;
    }

    public static StudentStore studentStore() {
        return studentStore;
    }

    public static TopicStore topicStore() {
        return topicStore;
    }

    public static SessionStore sessionStore() {
        return sessionStore;
    }

    public static QuestionHistoryStore historyStore() {
        return historyStore;
    }

    public static void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.out.println("Could not close the database connection: " + e.getMessage());
            }
            connection = null;
        }
    }
}