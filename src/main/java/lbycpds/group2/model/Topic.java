package lbycpds.group2.model;

public class Topic implements Comparable<Topic> {
    private String name;
    private String course;
    private int confidence;
    private int level;
    private int bestScore;
    private int latestScore;
    private boolean unlocked;

    public Topic(String name, String course) {
        this.name = name;
        this.course = course;
        this.confidence = 0;
        this.level = 1;
        this.bestScore = 0;
        this.latestScore = 0;
        this.unlocked = false;
    }

    public int compareTo(Topic other) {
        if (confidence != other.confidence) {
            return Integer.compare(confidence, other.confidence);
        }
        return Integer.compare(latestScore, other.latestScore);
    }

    public String getName() { return name; }
    public String getCourse() { return course; }
    public int getConfidence() { return confidence; }
    public int getLevel() { return level; }
    public int getBestScore() { return bestScore; }
    public int getLatestScore() { return latestScore; }
    public boolean isUnlocked() { return unlocked; }

    public void setConfidence(int confidence) { this.confidence = confidence; }
    public void setLevel(int level) { this.level = level; }
    public void setBestScore(int bestScore) { this.bestScore = bestScore; }
    public void setLatestScore(int latestScore) { this.latestScore = latestScore; }
    public void setUnlocked(boolean unlocked) { this.unlocked = unlocked; }
}
