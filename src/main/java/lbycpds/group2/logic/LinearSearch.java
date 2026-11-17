package lbycpds.group2.logic;

import lbycpds.group2.model.Question;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LinearSearch {

    public static List<Question> findByTopicAndLevel(Question[] questions, String course, String topic, int level) {
        List<Question> found = new ArrayList<>();
        for (int i = 0; i < questions.length; i++) {
            Question question = questions[i];
            if (question.getCourse().equals(course)
                    && question.getTopic().equals(topic)
                    && question.getDifficulty() == level) {
                found.add(question);
            }
        }
        return found;
    }

    public static Question findById(Question[] questions, String course, int id) {
        for (int i = 0; i < questions.length; i++) {
            if (questions[i].getId() == id && questions[i].getCourse().equals(course)) {
                return questions[i];
            }
        }
        return null;
    }

    public static List<Question> find(Question[] questions, String course, String topic, int level,
                                      List<Integer> recentIds) {
        List<Question> matches = findByTopicAndLevel(questions, course, topic, level);
        List<Question> fresh = new ArrayList<>();
        List<Question> recent = new ArrayList<>();
        for (int i = 0; i < matches.size(); i++) {
            if (containsId(recentIds, matches.get(i).getId())) {
                recent.add(matches.get(i));
            } else {
                fresh.add(matches.get(i));
            }
        }
        Collections.shuffle(fresh);
        Collections.shuffle(recent);
        fresh.addAll(recent);
        return fresh;
    }

    private static boolean containsId(List<Integer> ids, int id) {
        for (int i = 0; i < ids.size(); i++) {
            if (ids.get(i) == id) {
                return true;
            }
        }
        return false;
    }
}