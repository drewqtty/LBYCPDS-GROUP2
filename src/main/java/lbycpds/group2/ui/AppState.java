package lbycpds.group2.ui;

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

    public static Role role = Role.STUDENT;   // chosen on the first screen
    public static Student student;
    public static String mode = "REGULAR";   // regular or cram
    public static String course;             // only used in regular mode
    public static List<Topic> topics = new ArrayList<>();
    public static boolean returning = false;   // true if the student has saved data

    // When study session is running
    public static StudySession session;
    public static Map<String, Graph> graphs = new HashMap<>();   // topic order of each course
    public static LocalTopicStore topicStore;
    public static LocalSessionStore sessionStore;
    public static LocalHistoryStore historyStore;
}