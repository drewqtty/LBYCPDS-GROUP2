package lbycpds.group2.model;

import java.util.ArrayList;
import java.util.List;

// A lesson on one topic: a few pages of teaching material.
// practiceTopic is the question-bank topic used for the practice quiz ("" when there are no questions yet).
public class Lesson {
    private final String title;
    private final String category;
    private final String practiceTopic;
    private final List<LessonPage> pages = new ArrayList<>();

    public Lesson(String title, String category, String practiceTopic) {
        this.title = title;
        this.category = category;
        this.practiceTopic = practiceTopic;
    }

    public void addPage(LessonPage page) {
        pages.add(page);
    }

    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getPracticeTopic() { return practiceTopic; }
    public List<LessonPage> getPages() { return pages; }
    public int getPageCount() { return pages.size(); }
    public boolean hasPracticeTopic() { return !practiceTopic.isEmpty(); }
}