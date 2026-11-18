package lbycpds.group2.database;

public class LastSession {
    private final String course;
    private final String mode;
    private final String topic;   // null in cram mode

    public LastSession(String course, String mode, String topic) {
        this.course = course;
        this.mode = mode;
        this.topic = topic;
    }

    public String getCourse() { return course; }
    public String getMode() { return mode; }
    public String getTopic() { return topic; }
    public boolean isCram() { return "CRAM".equals(mode); }
}