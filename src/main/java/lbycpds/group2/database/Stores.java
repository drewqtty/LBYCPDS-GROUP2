package lbycpds.group2.database;

import java.sql.Connection;
import java.sql.SQLException;

public class Stores {
    private static Connection connection;
    private static StudentStore studentStore;
    private static AdminStore adminStore;
    private static CramStore cramStore;
    private static LastSessionStore lastSessionStore;
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
        try {
            lastSessionStore = new LastSessionDao(connection);
        } catch (IllegalStateException e) {
            lastSessionStore = null;
            System.out.println("The last session will not be saved: " + e.getMessage());
        }
        try {
            cramStore = new CramDao(connection);
        } catch (IllegalStateException e) {
            cramStore = null;
            System.out.println("Cram levels will not be saved: " + e.getMessage());
        }
        try {
            adminStore = new AdminDao(connection);
        } catch (IllegalStateException e) {
            adminStore = null;
            System.out.println("Admin names will not be saved: " + e.getMessage());
        }
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

    public static LastSessionStore lastSessionStore() {
        return lastSessionStore;
    }

    public static CramStore cramStore() {
        return cramStore;
    }

    public static AdminStore adminStore() {
        return adminStore;
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