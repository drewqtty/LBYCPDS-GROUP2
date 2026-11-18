package lbycpds.group2.database;

public interface LastSessionStore {
    // Remembers where the student is
    void save(String studentName, String course, String mode, String topic);

    // The student's last session
    LastSession load(String studentName);
}