package lbycpds.group2.test;

import lbycpds.group2.model.IdentificationQuestion;
import lbycpds.group2.model.MultipleChoiceQuestion;
import lbycpds.group2.model.ProblemSolvingQuestion;
import lbycpds.group2.model.Question;

public class QuestionTest {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("     RUNNING QUESTION MODEL TESTS       ");
        System.out.println("========================================\n");

        int passedTests = 0;
        int totalTests = 0;

        // --- TEST 1: Multiple Choice Question ---
        totalTests++;
        System.out.println("--- Test 1: Multiple Choice Question ---");
        String[] mcqChoices = { "Mesh Analysis", "Nodal Analysis", "Thevenin's Theorem" };
        Question mcq = new MultipleChoiceQuestion(
                1, "FUNDCK1", "Circuits", 1,
                "Which method uses KCL to find node voltages?",
                "Nodal Analysis",
                mcqChoices
        );

        printQuestionDetails(mcq);

        String ans1 = "Nodal Analysis";
        String ans2 = "  nodal analysis   ";
        String ans3 = "Mesh Analysis";

        boolean mcqTest1 = mcq.checkAnswer(ans1);      // Exact match (Expected: true)
        boolean mcqTest2 = mcq.checkAnswer(ans2);      // Case-insensitive & trimmed (Expected: true)
        boolean mcqTest3 = !mcq.checkAnswer(ans3);     // Incorrect choice check (Expected: true)

        printCheckResult(ans1, mcq.checkAnswer(ans1), true);
        printCheckResult(ans2, mcq.checkAnswer(ans2), true);
        printCheckResult(ans3, mcq.checkAnswer(ans3), false);

        if (mcqTest1 && mcqTest2 && mcqTest3) {
            System.out.println("RESULT: TEST 1 PASSED\n");
            passedTests++;
        } else {
            System.out.println("RESULT: TEST 1 FAILED\n");
        }


        // --- TEST 2: Problem Solving (Keyword-based) ---
        totalTests++;
        System.out.println("--- Test 2: Problem Solving (Keyword-based) ---");
        String[] keywords = { "KVL", "independent loops", "mesh currents" };
        ProblemSolvingQuestion essayProblem = new ProblemSolvingQuestion(
                2, "FUNDCK1", "Circuits", 2,
                "Explain the concept of Mesh Analysis.",
                "Mesh analysis applies Kirchhoff's Voltage Law to independent loops to find mesh currents.",
                keywords
        );

        printQuestionDetails(essayProblem);

        String essayAns1 = "It uses KVL on independent loops to solve for mesh currents.";
        String essayAns2 = "It uses KVL to analyze independent loops.";

        boolean essayTest1 = essayProblem.checkAnswer(essayAns1);  // Expected: true
        boolean essayTest2 = !essayProblem.checkAnswer(essayAns2); // Expected: true (should fail keywords)

        printCheckResult(essayAns1, essayProblem.checkAnswer(essayAns1), true);
        printCheckResult(essayAns2, essayProblem.checkAnswer(essayAns2), false);

        if (essayTest1 && essayTest2) {
            System.out.println("RESULT: TEST 2 PASSED\n");
            passedTests++;
        } else {
            System.out.println("RESULT: TEST 2 FAILED\n");
        }


        // --- TEST 3: Problem Solving (Numeric Tolerance) ---
        totalTests++;
        System.out.println("--- Test 3: Problem Solving (Numeric Tolerance) ---");
        ProblemSolvingQuestion calcProblem = new ProblemSolvingQuestion(
                3, "FUNDCK1", "Circuits", 3,
                "Calculate the total equivalent resistance (in ohms):",
                "50.0",
                0.05 // 5% tolerance
        );

        printQuestionDetails(calcProblem);

        String calcAns1 = "50.0";
        String calcAns2 = "51.5";
        String calcAns3 = "48.5V";
        String calcAns4 = "60.0";

        boolean calcTest1 = calcProblem.checkAnswer(calcAns1);  // Expected: true
        boolean calcTest2 = calcProblem.checkAnswer(calcAns2);  // Expected: true
        boolean calcTest3 = calcProblem.checkAnswer(calcAns3);  // Expected: true
        boolean calcTest4 = !calcProblem.checkAnswer(calcAns4); // Expected: true (should fail)

        printCheckResult(calcAns1, calcProblem.checkAnswer(calcAns1), true);
        printCheckResult(calcAns2, calcProblem.checkAnswer(calcAns2), true);
        printCheckResult(calcAns3, calcProblem.checkAnswer(calcAns3), true);
        printCheckResult(calcAns4, calcProblem.checkAnswer(calcAns4), false);

        if (calcTest1 && calcTest2 && calcTest3 && calcTest4) {
            System.out.println("RESULT: TEST 3 PASSED\n");
            passedTests++;
        } else {
            System.out.println("RESULT: TEST 3 FAILED\n");
        }


        // --- TEST 4: Identification Question ---
        totalTests++;
        System.out.println("--- Test 4: Identification Question ---");
        Question idQ = new IdentificationQuestion(
                4, "FUNDCK1", "Basics", 1,
                "What law states that voltage equals current times resistance?",
                "Ohm's Law"
        );

        printQuestionDetails(idQ);

        String idAns1 = "Ohm's Law";
        String idAns2 = "  ohm's law   ";
        String idAns3 = "Kirchhoff's Law";

        boolean idTest1 = idQ.checkAnswer(idAns1);     // Expected: true
        boolean idTest2 = idQ.checkAnswer(idAns2);     // Expected: true
        boolean idTest3 = !idQ.checkAnswer(idAns3);    // Expected: true (should fail)

        printCheckResult(idAns1, idQ.checkAnswer(idAns1), true);
        printCheckResult(idAns2, idQ.checkAnswer(idAns2), true);
        printCheckResult(idAns3, idQ.checkAnswer(idAns3), false);

        if (idTest1 && idTest2 && idTest3) {
            System.out.println("RESULT: TEST 4 PASSED\n");
            passedTests++;
        } else {
            System.out.println("RESULT: TEST 4 FAILED\n");
        }


        // --- Summary ---
        System.out.println("========================================");
        System.out.println("TEST RESULTS: " + passedTests + "/" + totalTests + " test suites passed.");
        System.out.println("========================================");
    }

    private static void printQuestionDetails(Question q) {
        System.out.println("Type     : " + q.getType());
        System.out.println("Question : " + q.getText());
        System.out.println("Answer   : " + q.getAnswer());
    }

    private static void printCheckResult(String studentAnswer, boolean actualResult, boolean expectedToBeCorrect) {
        boolean match = (actualResult == expectedToBeCorrect);
        System.out.println("  -> Student Answer: \"" + studentAnswer + "\" | Evaluated Correct? " + actualResult + " [" + (match ? "CORRECT" : "MISMATCH") + "]");
    }
}
