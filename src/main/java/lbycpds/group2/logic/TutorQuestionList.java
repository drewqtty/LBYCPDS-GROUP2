package lbycpds.group2.logic;

import lbycpds.group2.model.Question;

import java.util.ArrayList;
import java.util.List;


public class TutorQuestionList {

    public static List<String> topics(Question[] questions) {
        List<String> names = new ArrayList<>();
        for (Question q : questions) {
            if (!names.contains(q.getTopic())) {
                names.add(q.getTopic());
            }
        }
        return names;
    }

    public static List<Question> forTopic(Question[] questions, String topic) {
        List<String> order = topics(questions);
        List<Question> list = new ArrayList<>();
        for (Question q : questions) {
            if (topic == null || topic.isEmpty() || q.getTopic().equals(topic)) {
                list.add(q);
            }
        }
        list.sort((a, b) -> {
            int byTopic = Integer.compare(order.indexOf(a.getTopic()), order.indexOf(b.getTopic()));
            if (byTopic != 0) return byTopic;
            int byLevel = Integer.compare(a.getDifficulty(), b.getDifficulty());
            if (byLevel != 0) return byLevel;
            return Integer.compare(a.getId(), b.getId());
        });
        return list;
    }
}
