package lbycpds.group2.model;

public class ProblemSolvingQuestion extends Question {
    private String[] keywords;

    public ProblemSolvingQuestion(int id, String course, String topic, int difficulty,
                                  String text, String answer, String[] keywords) {
        super(id, course, topic, difficulty, text, answer);
        this.keywords = keywords;
    }

    public boolean checkAnswer(String studentAnswer) {
        String lowered = studentAnswer.toLowerCase();
        for (int i = 0; i < keywords.length; i++) {
            if (!lowered.contains(keywords[i].toLowerCase())) {
                return false;
            }
        }
        return true;
    }

    public String getType() {
        return "PROBLEM_SOLVING";
    }

    public String[] getKeywords() { return keywords; }
}
