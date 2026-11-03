package lbycpds.group2.database;

import lbycpds.group2.model.Student;

public interface StudentStore {
    Student loadStudent(String name);

    void saveStudent(Student student);
}
