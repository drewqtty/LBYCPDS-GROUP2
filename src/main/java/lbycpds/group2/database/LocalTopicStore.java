package lbycpds.group2.database;

import lbycpds.group2.model.Topic;

import java.util.ArrayList;
import java.util.List;

public class LocalTopicStore implements TopicStore {
    private final List<Topic> topics;
    private final TopicStore saved;

    public LocalTopicStore(List<Topic> topics, TopicStore saved) {
        this.topics = topics;
        this.saved = saved;
    }

    @Override
    public List<Topic> loadTopics(String studentName, String course) {
        List<Topic> found = new ArrayList<>();
        for (Topic topic : topics) {
            if (topic.getCourse().equals(course)) {
                found.add(topic);
            }
        }
        return found;
    }

    @Override
    public void saveLastTopic(String studentName, String course, String topic) {
        if (saved != null) {
            saved.saveLastTopic(studentName, course, topic);
        }
    }

    @Override
    public String loadLastTopic(String studentName, String course) {
        return saved != null ? saved.loadLastTopic(studentName, course) : null;
    }

    @Override
    public String loadLastCourse(String studentName) {
        return saved != null ? saved.loadLastCourse(studentName) : null;
    }

    @Override
    public void saveTopic(String studentName, Topic topic) {
        if (saved != null) {
            saved.saveTopic(studentName, topic);
        }
    }
}