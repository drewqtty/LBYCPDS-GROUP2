package lbycpds.group2.database;

import lbycpds.group2.model.Topic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TopicDao extends BaseDao implements TopicStore {

    public TopicDao(Connection connection) {
        super(connection);
    }

    public List<Topic> loadTopics(String studentName, String course) {
        List<Topic> topics = new ArrayList<>();
        String sql = "SELECT topic, confidence, level, best_score, latest_score, unlocked "
                + "FROM topic_progress WHERE student_name = ? AND course = ? "
                + "ORDER BY updated_at, topic";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, studentName);
            statement.setString(2, course);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    Topic topic = new Topic(result.getString("topic"), course);
                    topic.setConfidence(result.getInt("confidence"));
                    topic.setLevel(result.getInt("level"));
                    topic.setBestScore(result.getInt("best_score"));
                    topic.setLatestScore(result.getInt("latest_score"));
                    topic.setUnlocked(result.getBoolean("unlocked"));
                    topics.add(topic);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not load topics: " + e.getMessage());
        }
        return topics;
    }

    public void saveTopic(String studentName, Topic topic) {
        ensureCourse(studentName, topic.getCourse());
        update("INSERT INTO topic_progress (student_name, course, topic, confidence, level, "
                        + "best_score, latest_score, unlocked, updated_at) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, now()) "
                        + "ON CONFLICT (student_name, course, topic) DO UPDATE SET "
                        + "confidence = EXCLUDED.confidence, level = EXCLUDED.level, "
                        + "best_score = EXCLUDED.best_score, latest_score = EXCLUDED.latest_score, "
                        + "unlocked = EXCLUDED.unlocked, updated_at = now()",
                studentName, topic.getCourse(), topic.getName(), topic.getConfidence(),
                topic.getLevel(), topic.getBestScore(), topic.getLatestScore(), topic.isUnlocked());
    }

    public void saveLastTopic(String studentName, String course, String topic) {
        ensureCourse(studentName, course);
        update("UPDATE student_courses SET last_topic = ?, last_played_at = now() "
                        + "WHERE student_name = ? AND course = ?",
                topic, studentName, course);
    }

    public String loadLastTopic(String studentName, String course) {
        String sql = "SELECT last_topic FROM student_courses WHERE student_name = ? AND course = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, studentName);
            statement.setString(2, course);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getString("last_topic");
                }
                return null;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not load last topic: " + e.getMessage());
        }
    }
}