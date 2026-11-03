package lbycpds.group2.model;

import java.time.LocalDate;

public class Session {
    private String course;
    private LocalDate date;
    private int score;
    private int timeSpentSeconds;

    public Session(String course, LocalDate date, int score, int timeSpentSeconds) {
        this.course = course;
        this.date = date;
        this.score = score;
        this.timeSpentSeconds = timeSpentSeconds;
    }

    public String getCourse() { return course; }
    public LocalDate getDate() { return date; }
    public int getScore() { return score; }
    public int getTimeSpentSeconds() { return timeSpentSeconds; }
}