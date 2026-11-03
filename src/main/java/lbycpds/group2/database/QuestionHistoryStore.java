package lbycpds.group2.database;

import java.util.List;

public interface QuestionHistoryStore {
    void addMissed(String studentName, String course, int questionId);

    void removeMissed(String studentName, String course, int questionId);

    List<Integer> loadMissed(String studentName, String course);

    void addRecent(String studentName, String course, int questionId);

    List<Integer> loadRecent(String studentName, String course);
}
