package lbycpds.group2.structures;

import lbycpds.group2.model.Topic;
import java.util.List;

public interface ProgressionGraph extends DataStructure {
    void addTopic(Topic topic);

    void addEdge(String fromTopic, String toTopic);

    List<Topic> unlockNext(String topicName);
}