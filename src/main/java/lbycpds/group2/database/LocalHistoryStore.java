package lbycpds.group2.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class LocalHistoryStore implements QuestionHistoryStore {
    private static final int MAX_RECENT = 50;

    private final Map<String, List<Integer>> missed = new HashMap<>();
    private final Map<String, List<Integer>> recent = new HashMap<>();
    private final QuestionHistoryStore saved;

    public LocalHistoryStore(QuestionHistoryStore saved) {
        this.saved = saved;
    }

    @Override
    public void addMissed(String studentName, String course, int questionId) {
        List<Integer> list = missed.computeIfAbsent(key(studentName, course), k -> new ArrayList<>());
        if (!list.contains(questionId)) {
            list.add(questionId);
        }
        if (saved != null) {
            saved.addMissed(studentName, course, questionId);
        }
    }

    @Override
    public void removeMissed(String studentName, String course, int questionId) {
        List<Integer> list = missed.get(key(studentName, course));
        if (list != null) {
            list.remove(Integer.valueOf(questionId));
        }
        if (saved != null) {
            saved.removeMissed(studentName, course, questionId);
        }
    }

    @Override
    public List<Integer> loadMissed(String studentName, String course) {
        if (saved != null) {
            return new ArrayList<>(saved.loadMissed(studentName, course));
        }
        return new ArrayList<>(missed.getOrDefault(key(studentName, course), new ArrayList<>()));
    }

    @Override
    public void addRecent(String studentName, String course, int questionId) {
        List<Integer> list = recent.computeIfAbsent(key(studentName, course), k -> new ArrayList<>());
        list.remove(Integer.valueOf(questionId));
        list.add(questionId);
        while (list.size() > MAX_RECENT) {
            list.remove(0);
        }
        if (saved != null) {
            saved.addRecent(studentName, course, questionId);
        }
    }

    @Override
    public List<Integer> loadRecent(String studentName, String course) {
        if (saved != null) {
            return new ArrayList<>(saved.loadRecent(studentName, course));
        }
        return new ArrayList<>(recent.getOrDefault(key(studentName, course), new ArrayList<>()));
    }

    private static String key(String studentName, String course) {
        return studentName + "_" + course;
    }
}
