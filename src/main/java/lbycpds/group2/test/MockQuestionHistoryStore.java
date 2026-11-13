package lbycpds.group2.test;
import lbycpds.group2.database.QuestionHistoryStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MockQuestionHistoryStore implements QuestionHistoryStore {
    private final Map<String, List<Integer>> missedStore = new HashMap<>();
    private final Map<String, List<Integer>> recentStore = new HashMap<>();

    @Override
    public void addMissed(String studentName, String course, int questionId) {
        missedStore.computeIfAbsent(studentName + "_" + course, k -> new ArrayList<>()).add(questionId);
    }

    @Override
    public void removeMissed(String studentName, String course, int questionId) {
        List<Integer> list = missedStore.get(studentName + "_" + course);
        if (list != null) list.remove(Integer.valueOf(questionId));
    }

    @Override
    public List<Integer> loadMissed(String studentName, String course) {
        return missedStore.getOrDefault(studentName + "_" + course, new ArrayList<>());
    }

    @Override
    public void addRecent(String studentName, String course, int questionId) {
        recentStore.computeIfAbsent(studentName + "_" + course, k -> new ArrayList<>()).add(questionId);
    }

    @Override
    public List<Integer> loadRecent(String studentName, String course) {
        return recentStore.getOrDefault(studentName + "_" + course, new ArrayList<>());
    }
}