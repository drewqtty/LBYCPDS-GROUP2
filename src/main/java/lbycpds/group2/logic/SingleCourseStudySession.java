package lbycpds.group2.logic;

import lbycpds.group2.database.QuestionHistoryStore;
import lbycpds.group2.database.SessionStore;
import lbycpds.group2.database.TopicStore;
import lbycpds.group2.model.Question;
import lbycpds.group2.model.Student;
import lbycpds.group2.model.Topic;
import lbycpds.group2.structures.Graph;
import lbycpds.group2.structures.MinHeapTree;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class SingleCourseStudySession extends StudySession {
    private final String targetCourse;
    private final DifficultyPolicy difficultyPolicy;
    private final Graph progressionGraph;

    // Topic to study first when the student continues a saved session
    private String startTopic;
    private int sessionScore = 0;
    private int sessionTotalQuestions = 0;
    private long turnStartTime;

    private Topic activeTopic;

    public SingleCourseStudySession(Student student,
                                    TopicStore topicStore,
                                    SessionStore sessionStore,
                                    QuestionHistoryStore historyStore,
                                    String targetCourse,
                                    DifficultyPolicy difficultyPolicy,
                                    Graph progressionGraph) {
        super(student, topicStore, sessionStore, historyStore);
        this.targetCourse = targetCourse;
        this.difficultyPolicy = difficultyPolicy;
        this.progressionGraph = progressionGraph;
        this.turnStartTime = System.currentTimeMillis();
    }

    @Override
    public String nextCourse() {
        return this.targetCourse;
    }

    // Makes the next turn start on this topic (used when continuing a saved session)
    public void setStartTopic(String topicName) {
        this.startTopic = topicName;
    }

    @Override
    public Topic getTargetTopic() {
        activeTopic = pickTopic();
        return activeTopic;
    }

    private Topic pickTopic() {
        if (topicStore == null) return null;

        List<Topic> topics = topicStore.loadTopics(student.getName(), targetCourse);
        if (topics == null || topics.isEmpty()) return null;

        if (startTopic != null) {
            for (Topic topic : topics) {
                if (topic.isUnlocked() && topic.getName().equals(startTopic)) {
                    return topic;
                }
            }
        }

        List<Topic> unlockedTopics = topics.stream()
                .filter(Topic::isUnlocked)
                .collect(Collectors.toList());

        if (unlockedTopics.isEmpty()) {
            return null;
        }

        Topic[] topicArray = unlockedTopics.toArray(new Topic[0]);
        MinHeapTree heap = new MinHeapTree();
        heap.createMinHeapTree(topicArray);

        if (heap.root != null) {
            return heap.root.data;
        }

        return unlockedTopics.get(0);
    }

    public void recordAnswer(Question question, String studentAnswer) {
        if (question == null) return;

        boolean isCorrect = question.checkAnswer(studentAnswer);
        int questionId = question.getId();

        if (sessionTotalQuestions == 0) {
            turnStartTime = System.currentTimeMillis();
        }
        this.sessionTotalQuestions++;
        if (isCorrect) {
            this.sessionScore++;
            //If the user got it right, remove it from missed history
            if (historyStore != null) {
                historyStore.removeMissed(student.getName(), targetCourse, questionId);
            }
        } else {
            //If the user got it wrong, add it to missed history
            if (historyStore != null) {
                historyStore.addMissed(student.getName(), targetCourse, questionId);
            }
        }

        //Always record it as a recently answered question
        if (historyStore != null) {
            historyStore.addRecent(student.getName(), targetCourse, questionId);
        }
    }

    @Override
    public void finishTurn(String course) {
        if (!this.targetCourse.equalsIgnoreCase(course) || sessionTotalQuestions == 0) {
            return;
        }

        long elapsedTimeMs = System.currentTimeMillis() - turnStartTime;
        int timeSpentSeconds = (int) (elapsedTimeMs / 1000);
        int percent = Math.round(100f * sessionScore / sessionTotalQuestions);

        //Fetch the current weak topic using the MinHeapTree logic
        Topic currentTopic = activeTopic != null ? activeTopic : pickTopic();
        if (currentTopic != null) {
            int currentLevel = currentTopic.getLevel();
            int nextLevel = difficultyPolicy.nextLevel(currentLevel, sessionScore, sessionTotalQuestions);

            currentTopic.setLevel(nextLevel);
            currentTopic.setLatestScore(percent);
            if (percent > currentTopic.getBestScore()) {
                currentTopic.setBestScore(percent);
            }

            if (progressionGraph != null && currentTopic.getLatestScore() >= StudyFlow.PASS_PERCENT) {
                progressionGraph.unlockNext(currentTopic.getName());
            }
        }

        saveProgress(currentTopic, timeSpentSeconds, percent);

        this.sessionScore = 0;
        this.sessionTotalQuestions = 0;
        this.startTopic = null;   // later turns go back to the weakest topic
        this.turnStartTime = System.currentTimeMillis();
    }

    private void saveProgress(Topic currentTopic, int timeSpentSeconds, int percent) {
        if (topicStore != null && currentTopic != null) {
            topicStore.saveTopic(student.getName(), currentTopic);
        }
        if (sessionStore != null) {
            var sessionRecord = new lbycpds.group2.model.Session(
                    targetCourse,
                    LocalDate.now(),
                    percent,
                    timeSpentSeconds
            );
            sessionStore.saveSession(student.getName(), sessionRecord);
        }
    }
}