package lbycpds.group2.test;
import lbycpds.group2.database.TopicStore;
import lbycpds.group2.model.Topic;

import java.util.*;

public class MockTopicStore implements TopicStore {
    private final Map<String, List<Topic>> topicStoreMap = new HashMap<>();

    public void setTopicsForCourse(String studentName, String course, List<Topic> topics) {
        topicStoreMap.put(studentName + "_" + course, topics);
    }

    @Override
    public List<Topic> loadTopics(String studentName, String course) {
        return topicStoreMap.getOrDefault(studentName + "_" + course, new ArrayList<>());
    }

    @Override
    public void saveTopic(String studentName, Topic topic) {
        String key = studentName + "_" + topic.getCourse();
        List<Topic> topics = topicStoreMap.get(key);
        if (topics != null) {
            for (int i = 0; i < topics.size(); i++) {
                if (topics.get(i).getName().equalsIgnoreCase(topic.getName())) {
                    topics.set(i, topic);
                    break;
                }
            }
        }
    }
}