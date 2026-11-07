package lbycpds.group2.ui;

import lbycpds.group2.model.Student;
import lbycpds.group2.model.Topic;
import java.util.ArrayList;
import java.util.List;

public class AppState {
    public static Student student;
    public static String mode = "REGULAR";   // "REGULAR" or "CRAM"
    public static String course;             // only used in REGULAR mode
    public static List<Topic> topics = new ArrayList<>();
    public static boolean returning = false;   // true if the student has saved data
}