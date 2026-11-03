package lbycpds.group2.model;

public abstract class Question {
    private int id;
    private String course;
    private String topic;
    private int difficulty;
    private String text;
    private String answer;

    public Question(int id, String course, String topic, int difficulty, String text, String answer) {
        this.id = id;
        this.course = course;
        this.topic = topic;
        this.difficulty = difficulty;
        this.text = text;
        this.answer = answer;
    }

    public abstract boolean checkAnswer(String studentAnswer);

    public abstract String getType();

    protected boolean isExactMatch(String studentAnswer) {
        return answer.trim().equalsIgnoreCase(studentAnswer.trim());
    }

    public int getId() { return id; }
    public String getCourse() { return course; }
    public String getTopic() { return topic; }
    public int getDifficulty() { return difficulty; }
    public String getText() { return text; }
    public String getAnswer() { return answer; }
}