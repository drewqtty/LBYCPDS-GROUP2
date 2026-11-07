package lbycpds.group2.logic.checkers;


public class NumericToleranceChecker implements SpecialChecker {
    private double tolerancePercentage;

    public NumericToleranceChecker(double tolerancePercentage) {
        this.tolerancePercentage = tolerancePercentage;
    }

    //default constructor with a 1% error margin
    public NumericToleranceChecker() {
        this.tolerancePercentage = 0.01;
    }

    @Override
    public boolean check(String studentAnswer, String correctAnswer) {
        if (studentAnswer == null || correctAnswer == null) {
            return false;
        }

        try {
            //keep only numbers, decimals, and negative signs
            String cleanStudent = studentAnswer.trim().replaceAll("[^0-9.-]", "");
            String cleanCorrect = correctAnswer.trim().replaceAll("[^0-9.-]", "");

            double studentVal = Double.parseDouble(cleanStudent);
            double correctVal = Double.parseDouble(cleanCorrect);

            //handle edge case where correct value is zero
            if (correctVal == 0.0) {
                return Math.abs(studentVal) <= 0.0001;
            }

            //check if the difference falls within the tolerance range
            double diff = Math.abs(studentVal - correctVal);
            double allowedDiff = Math.abs(correctVal * tolerancePercentage);

            return diff <= allowedDiff;

        } catch (NumberFormatException e) {
            //if the answer isn't a valid number, do a standard string comparison
            return studentAnswer.trim().equalsIgnoreCase(correctAnswer.trim());
        }
    }
}

