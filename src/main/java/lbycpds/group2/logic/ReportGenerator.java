package lbycpds.group2.logic;

import lbycpds.group2.model.Student;

public interface ReportGenerator {
    String generate(Student student, String course);
}