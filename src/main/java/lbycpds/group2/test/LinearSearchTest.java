package lbycpds.group2.test;

import lbycpds.group2.logic.LinearSearch;
import lbycpds.group2.logic.TextFileQuestionBank;
import lbycpds.group2.model.Question;

import java.util.ArrayList;
import java.util.List;

public class LinearSearchTest {
    private static final String CIRCUITS = "Fundamentals of Electrical Circuits";
    private static final String DSA = "Data Structures and Algorithms";

    public static void main(String[] args) {
        Question[] questions = new TextFileQuestionBank("/questions/circuits.txt").loadQuestions();

        List<Question> part = LinearSearch.findByTopicAndLevel(questions, CIRCUITS, "Resistance", 2);
        check("a topic at one level has 3 questions", part.size() == 3);
        boolean allMatch = true;
        for (int i = 0; i < part.size(); i++) {
            if (!part.get(i).getTopic().equals("Resistance") || part.get(i).getDifficulty() != 2) {
                allMatch = false;
            }
        }
        check("every result matches the topic and level", allMatch);
        check("an unknown topic returns nothing",
                LinearSearch.findByTopicAndLevel(questions, CIRCUITS, "No Such Topic", 1).isEmpty());
        check("a different course returns nothing",
                LinearSearch.findByTopicAndLevel(questions, DSA, "Resistance", 1).isEmpty());

        Question byId = LinearSearch.findById(questions, CIRCUITS, 111);
        check("a question is found by its id",
                byId != null && byId.getTopic().equals("Resistance") && byId.getDifficulty() == 1);
        check("an unknown id returns null", LinearSearch.findById(questions, CIRCUITS, 999999) == null);

        List<Integer> recent = new ArrayList<>();
        recent.add(121);
        List<Question> ordered = LinearSearch.find(questions, CIRCUITS, "Resistance", 2, recent);
        check("recent questions are kept, not dropped", ordered.size() == 3);
        check("the recently answered question comes last", ordered.get(2).getId() == 121);

        recent.add(122);
        recent.add(123);
        check("when every question is recent, none are lost",
                LinearSearch.find(questions, CIRCUITS, "Resistance", 2, recent).size() == 3);
    }

    private static void check(String label, boolean ok) {
        System.out.println((ok ? "PASS: " : "FAIL: ") + label);
    }
}