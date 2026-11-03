package lbycpds.group2.logic;

import lbycpds.group2.model.Question;

public interface QuestionSource {
    String getCourse();

    Question[] loadQuestions();

    String[][] loadTopicLinks();
}