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

    private int sessionScore = 0;
    private int sessionTotalQuestions = 0;
    private long turnStartTime;

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

    public Topic getTargetTopic() {
        if (topicStore == null) return null;

        List<Topic> topics = topicStore.loadTopics(student.getName(), targetCourse);
        if (topics == null || topics.isEmpty()) return null;

        List<Topic> unlockedTopics = topics.stream()
                .filter(Topic::isUnlocked)
                .collect(Collectors.toList());

        if (unlockedTopics.isEmpty()) {
            return topics.get(0);
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

        //Fetch the current weak topic using the MinHeapTree logic
        Topic currentTopic = getTargetTopic();
        if (currentTopic != null) {
            int currentLevel = currentTopic.getLevel();
            int nextLevel = difficultyPolicy.nextLevel(currentLevel, sessionScore, sessionTotalQuestions);

            currentTopic.setLevel(nextLevel);
            currentTopic.setLatestScore(sessionScore);
            if (sessionScore > currentTopic.getBestScore()) {
                currentTopic.setBestScore(sessionScore);
            }

            if (progressionGraph != null && currentTopic.getLatestScore() >= 70) {
                progressionGraph.unlockNext(currentTopic.getName());
            }
        }

        saveProgress(timeSpentSeconds);

        this.sessionScore = 0;
        this.sessionTotalQuestions = 0;
        this.turnStartTime = System.currentTimeMillis();
    }

    private void saveProgress(int timeSpentSeconds) {
        if (topicStore != null) {
            Topic currentTopic = getTargetTopic();
            if (currentTopic != null) {
                topicStore.saveTopic(student.getName(), currentTopic);
            }
        }
        if (sessionStore != null) {
            var sessionRecord = new lbycpds.group2.model.Session(
                    targetCourse,
                    LocalDate.now(),
                    sessionScore,
                    timeSpentSeconds
            );
            sessionStore.saveSession(student.getName(), sessionRecord);
        }
    }
}