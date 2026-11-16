package lbycpds.group2.database;

import lbycpds.group2.model.Topic;
import java.util.List;

public interface TopicStore {
    List<Topic> loadTopics(String studentName, String course);

    void saveTopic(String studentName, Topic topic);

    default void saveLastTopic(String studentName, String course, String topic) {
    }

    default String loadLastTopic(String studentName, String course) {
        return null;
    }
}