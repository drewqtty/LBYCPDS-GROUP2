package lbycpds.group2.model;

public class IdentificationQuestion extends Question {

    public IdentificationQuestion(int id, String course, String topic, int difficulty,
                                  String text, String answer) {
        super(id, course, topic, difficulty, text, answer);
    }

    public boolean checkAnswer(String studentAnswer) {
        return isExactMatch(studentAnswer);
    }

    public String getType() {
        return "IDENTIFICATION";
    }
}
