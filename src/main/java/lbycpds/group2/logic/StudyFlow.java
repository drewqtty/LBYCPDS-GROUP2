package lbycpds.group2.logic;

import lbycpds.group2.model.Question;
import lbycpds.group2.model.Topic;
import lbycpds.group2.structures.Graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class StudyFlow {

    public static final int PASS_PERCENT = 70;

    public static Map<String, Graph> buildGraphs(List<Topic> topics, List<? extends QuestionSource> banks) {
        Map<String, Graph> graphs = new HashMap<>();
        for (QuestionSource bank : banks) {
            String course = bank.getCourse();
            Graph graph = new Graph();
            List<Topic> courseTopics = new ArrayList<>();
            for (Topic topic : topics) {
                if (topic.getCourse().equals(course)) {
                    graph.addTopic(topic);
                    courseTopics.add(topic);
                }
            }
            if (courseTopics.isEmpty()) {
                continue;
            }

            for (String[] link : bank.loadTopicLinks()) {
                for (int i = 1; i < link.length; i++) {
                    graph.addEdge(link[0], link[i]);
                }
            }

            graph.unlockRoots();
            for (Topic topic : courseTopics) {
                if (topic.getLatestScore() >= PASS_PERCENT) {
                    topic.setUnlocked(true);
                    graph.unlockNext(topic.getName());
                }
            }
            graphs.put(course, graph);
        }
        return graphs;
    }


    public static List<Question> pickQuestions(Question[] bank, Topic topic, List<Integer> recentIds, int max) {
        int level = Math.max(1, Math.min(3, topic.getLevel()));
        int[] levelOrder;
        if (level == 1) {
            levelOrder = new int[]{1, 2, 3};
        } else if (level == 2) {
            levelOrder = new int[]{2, 1, 3};
        } else {
            levelOrder = new int[]{3, 2, 1};
        }

        List<Question> picked = new ArrayList<>();
        for (int i = 0; i < levelOrder.length && picked.size() < max; i++) {
            List<Question> found = LinearSearch.find(bank, topic.getCourse(), topic.getName(),
                    levelOrder[i], recentIds);
            for (int j = 0; j < found.size() && picked.size() < max; j++) {
                picked.add(found.get(j));
            }
        }
        return picked;
    }


    public static List<Question> missedQuestions(Question[] bank, String course, List<Integer> missedIds) {
        List<Question> found = new ArrayList<>();
        for (int i = 0; i < missedIds.size(); i++) {
            Question question = LinearSearch.findById(bank, course, missedIds.get(i));
            if (question != null) {
                found.add(question);
            }
        }
        return found;
    }
}
