package lbycpds.group2.logic;

import lbycpds.group2.database.SessionStore;
import lbycpds.group2.database.TopicStore;
import lbycpds.group2.model.Session;
import lbycpds.group2.model.Student;
import lbycpds.group2.model.Topic;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a plain-text progress report. Topics are listed weakest first
 * using the group's InsertionSort (lowest confidence, then lowest latest score).
 */
public class TextReportGenerator implements ReportGenerator {

    private final TopicStore topicStore;
    private final SessionStore sessionStore;

    public TextReportGenerator(TopicStore topicStore, SessionStore sessionStore) {
        this.topicStore = topicStore;
        this.sessionStore = sessionStore;
    }

    @Override
    public String generate(Student student, String course) {
        List<Topic> topics = new ArrayList<>(topicStore.loadTopics(student.getName(), course));
        InsertionSort.sort(topics);
        List<Session> sessions = sessionStore.loadSessions(student.getName(), course);
        return build(student.getName(), course, topics, sessions);
    }

    /** Same report from data you already have (no database needed). */
    public static String build(String studentName, String course, List<Topic> sortedTopics, List<Session> sessions) {
        StringBuilder sb = new StringBuilder();
        sb.append("PROGRESS REPORT\n");
        sb.append("Student: ").append(studentName).append("\n");
        sb.append("Course:  ").append(course).append("\n\n");

        sb.append("Sessions completed: ").append(sessions.size()).append("\n");
        sb.append("Total study time:   ").append(formatTime(totalSeconds(sessions))).append("\n");
        sb.append("Average score:     ").append(averageScore(sessions)).append("%\n\n");

        sb.append("TOPICS (weakest first)\n");
        int rank = 1;
        for (Topic t : sortedTopics) {
            sb.append(rank++).append(". ").append(t.getName())
                    .append("  |  confidence ").append(t.getConfidence()).append("/5")
                    .append("  |  level ").append(t.getLevel())
                    .append("  |  latest ").append(t.getLatestScore()).append("%")
                    .append("  |  best ").append(t.getBestScore()).append("%\n");
        }
        if (!sortedTopics.isEmpty()) {
            sb.append("\nFocus next on: ").append(sortedTopics.get(0).getName()).append("\n");
        }
        return sb.toString();
    }

    public static int totalSeconds(List<Session> sessions) {
        int total = 0;
        for (Session s : sessions) total += s.getTimeSpentSeconds();
        return total;
    }

    public static int averageScore(List<Session> sessions) {
        if (sessions.isEmpty()) return 0;
        int sum = 0;
        for (Session s : sessions) sum += s.getScore();
        return Math.round((float) sum / sessions.size());
    }

    /** 125 -> "2m 05s", 3700 -> "1h 01m". */
    public static String formatTime(int seconds) {
        int h = seconds / 3600, m = (seconds % 3600) / 60, s = seconds % 60;
        if (h > 0) return String.format("%dh %02dm", h, m);
        return String.format("%dm %02ds", m, s);
    }
}