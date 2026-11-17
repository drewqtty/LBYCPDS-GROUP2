package lbycpds.group2.database;

import lbycpds.group2.model.Topic;

import java.util.List;

public class ProgressRestorer {

    public static void restore(TopicStore store, String studentName, List<Topic> topics) {
        if (topics.isEmpty()) {
            return;
        }
        List<Topic> saved = store.loadTopics(studentName, topics.get(0).getCourse());
        for (int i = 0; i < topics.size(); i++) {
            Topic fresh = topics.get(i);
            for (int j = 0; j < saved.size(); j++) {
                Topic old = saved.get(j);
                if (old.getName().equals(fresh.getName())) {
                    fresh.setConfidence(old.getConfidence());
                    fresh.setLevel(old.getLevel());
                    fresh.setBestScore(old.getBestScore());
                    fresh.setLatestScore(old.getLatestScore());
                    fresh.setUnlocked(old.isUnlocked());
                }
            }
        }
    }
}