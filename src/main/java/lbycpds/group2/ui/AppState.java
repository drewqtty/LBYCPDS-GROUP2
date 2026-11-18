package lbycpds.group2.ui;

import lbycpds.group2.database.LocalCramStore;
import lbycpds.group2.database.LocalHistoryStore;
import lbycpds.group2.database.LocalSessionStore;
import lbycpds.group2.database.LocalTopicStore;
import lbycpds.group2.logic.StudySession;
import lbycpds.group2.model.Student;
import lbycpds.group2.model.Topic;
import lbycpds.group2.structures.Graph;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppState {
    public enum Role { STUDENT, ADMIN }

    public static Role role = Role.STUDENT;
    public static Student student;
    public static String adminName;          // name the admin logged in with
    public static String mode = "REGULAR";   // regular or cram
    public static String course;             // only used in regular mode
    public static List<Topic> topics = new ArrayList<>();
    public static boolean returning = false;   // true if the student has saved data
    public static String resumeTopic;          // topic to start on when continuing a saved session

    // When study session is running
    public static StudySession session;
    public static Map<String, Graph> graphs = new HashMap<>();   // topic order of each course
    public static LocalTopicStore topicStore;
    public static LocalSessionStore sessionStore;
    public static LocalHistoryStore historyStore;
    public static LocalCramStore cramStore;

    public static void reset() {
        role = Role.STUDENT;
        student = null;
        adminName = null;
        mode = "REGULAR";
        course = null;
        topics = new ArrayList<>();
        returning = false;
        resumeTopic = null;
        session = null;
        graphs = new HashMap<>();
        topicStore = null;
        sessionStore = null;
        historyStore = null;
        cramStore = null;
    }
}