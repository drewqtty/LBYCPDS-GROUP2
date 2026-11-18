package lbycpds.group2.database;

public interface CramStore {
    int loadLevel(String studentName, String course);

    void saveResult(String studentName, String course, int level, int percent);
}