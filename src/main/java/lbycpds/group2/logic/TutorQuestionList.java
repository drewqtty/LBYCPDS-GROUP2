package lbycpds.group2.logic;

import lbycpds.group2.model.Question;

import java.util.ArrayList;
import java.util.List;

// Picks and orders the questions shown in Tutor mode.
// Questions are grouped by topic (in the order the topics first appear in the file),
// then go from easy to hard within each topic.
public class TutorQuestionList {

    // The distinct topic names, in file order
    public static List<String> topics(Question[] questions) {
        List<String> names = new ArrayList<>();
        for (Question q : questions) {
            if (!names.contains(q.getTopic())) {
                names.add(q.getTopic());
            }
        }
        return names;
    }

    // The questions of one topic, or of every topic when topic is null or empty
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
