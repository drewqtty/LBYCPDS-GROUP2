package lbycpds.group2.model;

import java.util.ArrayList;
import java.util.List;

public class Student {
    private String name;
    private List<String> courses;

    public Student(String name) {
        this.name = name;
        this.courses = new ArrayList<>();
    }

    public String getName() { return name; }
    public List<String> getCourses() { return courses; }

    public void addCourse(String course) {
        if (!courses.contains(course)) {
            courses.add(course);
        }
    }
}
