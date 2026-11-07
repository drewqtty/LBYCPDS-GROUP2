package lbycpds.group2.model;


import lbycpds.group2.logic.checkers.NumericToleranceChecker;
import lbycpds.group2.logic.checkers.SpecialChecker;

public class ProblemSolvingQuestion extends Question {
    private String[] keywords;
    private SpecialChecker checker;
    private boolean isNumeric;

    //Constructor for keyword-based problem solving
    public ProblemSolvingQuestion(int id, String course, String topic, int difficulty,
                                  String text, String answer, String[] keywords) {
        super(id, course, topic, difficulty, text, answer);
        this.keywords = keywords != null ? keywords : new String[0];
        this.isNumeric = false;
    }

    //Constructor for numeric tolerance problem solving
    public ProblemSolvingQuestion(int id, String course, String topic, int difficulty,
                                  String text, String answer, double tolerancePercentage) {
        super(id, course, topic, difficulty, text, answer);
        this.checker = new NumericToleranceChecker(tolerancePercentage);
        this.keywords = new String[0];
        this.isNumeric = true;
    }


    @Override
    public boolean checkAnswer(String studentAnswer) {
        if (studentAnswer == null) return false;

        if (isNumeric) {
            return checker.check(studentAnswer, getAnswer());
        } else {
            String lowered = studentAnswer.toLowerCase();
            for (String keyword : keywords) {
                if (!lowered.contains(keyword.toLowerCase())) {
                    return false;
                }
            }
            return true;
        }
    }

    @Override
    public String getType() {
        return "PROBLEM_SOLVING";
    }

    public String[] getKeywords() {
        return keywords;
    }
}
