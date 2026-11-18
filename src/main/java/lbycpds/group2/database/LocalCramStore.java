package lbycpds.group2.database;

import java.util.HashMap;
import java.util.Map;

public class LocalCramStore implements CramStore {
    private final Map<String, Integer> levels = new HashMap<>();
    private final CramStore saved;   // null when there is no database

    public LocalCramStore(CramStore saved) {
        this.saved = saved;
    }

    @Override
    public int loadLevel(String studentName, String course) {
        String key = studentName + "|" + course;
        if (!levels.containsKey(key)) {
            int level = 1;
            if (saved != null) {
                try {
                    level = saved.loadLevel(studentName, course);
                } catch (RuntimeException e) {
                    System.out.println("Could not load the cram level: " + e.getMessage());
                }
            }
            levels.put(key, level);
        }
        return levels.get(key);
    }

    @Override
    public void saveResult(String studentName, String course, int level, int percent) {
        levels.put(studentName + "|" + course, level);
        if (saved != null) {
            try {
                saved.saveResult(studentName, course, level, percent);
            } catch (RuntimeException e) {
                System.out.println("Could not save the cram level: " + e.getMessage());
            }
        }
    }
}