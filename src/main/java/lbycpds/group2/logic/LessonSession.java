package lbycpds.group2.logic;

import lbycpds.group2.model.Lesson;
import lbycpds.group2.model.LessonPage;

// Where a student is inside one lesson. The lesson screen asks this class which page to show and
// which buttons to enable, so the page-by-page rules live here and can be tested without JavaFX.
public class LessonSession {

    private final Lesson lesson;
    private int index = 0;

    public LessonSession(Lesson lesson) {
        if (lesson == null) {
            throw new IllegalArgumentException("A lesson is needed");
        }
        this.lesson = lesson;
    }

    public Lesson getLesson() {
        return lesson;
    }

    // False for a lesson whose pages are missing or all empty
    public boolean hasContent() {
        return lesson.getPageCount() > 0;
    }

    public int getPageCount() {
        return lesson.getPageCount();
    }

    // Page number as shown to the student (1 is the first page); 0 when the lesson has no pages
    public int getPageNumber() {
        return hasContent() ? index + 1 : 0;
    }

    // The page being shown, or null when the lesson has no pages
    public LessonPage currentPage() {
        return hasContent() ? lesson.getPages().get(index) : null;
    }

    public boolean hasPrevious() {
        return index > 0;
    }

    // True on the last page, where Next becomes Finish. A lesson with no pages counts as finished.
    public boolean isLastPage() {
        return index >= lesson.getPageCount() - 1;
    }

    // Moves to the next page. Does nothing and returns false on the last page.
    public boolean next() {
        if (isLastPage()) {
            return false;
        }
        index++;
        return true;
    }

    // Moves to the previous page. Does nothing and returns false on the first page.
    public boolean previous() {
        if (!hasPrevious()) {
            return false;
        }
        index--;
        return true;
    }

    // How far through the lesson the student is: 0.0 to 1.0. The first page of 5 is 0.2, the last page is 1.0.
    public double progress() {
        return hasContent() ? (double) (index + 1) / lesson.getPageCount() : 0.0;
    }
}