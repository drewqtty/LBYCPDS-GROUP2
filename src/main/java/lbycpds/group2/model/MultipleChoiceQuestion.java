package lbycpds.group2.model;

public class MultipleChoiceQuestion extends Question {
    private String[] choices;

    public MultipleChoiceQuestion(int id, String course, String topic, int difficulty,
                                  String text, String answer, String[] choices) {
        super(id, course, topic, difficulty, text, answer);
        this.choices = choices;
    }

    public boolean checkAnswer(String studentAnswer) {
        return isExactMatch(studentAnswer);
    }

    public String getType() {
        return "MULTIPLE_CHOICE";
    }

    public String[] getChoices() { return choices; }
}