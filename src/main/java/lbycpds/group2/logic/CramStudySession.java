package lbycpds.group2.logic;

import lbycpds.group2.database.QuestionHistoryStore;
import lbycpds.group2.database.SessionStore;
import lbycpds.group2.database.TopicStore;
import lbycpds.group2.model.Question;
import lbycpds.group2.model.Student;
import lbycpds.group2.model.Topic;
import lbycpds.group2.model.Session;
import lbycpds.group2.structures.CircularQueue;
import lbycpds.group2.structures.Graph;
import lbycpds.group2.structures.MinHeapTree;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CramStudySession extends StudySession {
    private final CircularQueue<String> courseQueue;
    private final DifficultyPolicy difficultyPolicy;
    private final Map<String, Graph> courseGraphs;

    private int sessionScore = 0;
    private int sessionTotalQuestions = 0;
    private long turnStartTime;

    public CramStudySession(Student student,
                            TopicStore topicStore,
                            SessionStore sessionStore,
                            QuestionHistoryStore historyStore,
                            DifficultyPolicy difficultyPolicy,
                            Map<String, Graph> courseGraphs) {
        super(student, topicStore, sessionStore, historyStore);

        List<String> studentCourses = student.getCourses();
        if (studentCourses == null || studentCourses.isEmpty()) {
            throw new IllegalArgumentException("Cram mode requires a non-empty course queue.");
        }

        this.courseQueue = new CircularQueue<>(studentCourses.size());
        for (String course : studentCourses) {
            this.courseQueue.offer(course);
        }

        this.difficultyPolicy = difficultyPolicy;
        this.courseGraphs = courseGraphs;
        this.turnStartTime = System.currentTimeMillis();
    }

    @Override
    public String nextCourse() {
        return courseQueue.front();
    }


    public Topic getCurrentTargetTopic() {
        if (topicStore == null) return null;
        String course = nextCourse();

        List<Topic> topics = topicStore.loadTopics(student.getName(), course);
        if (topics == null || topics.isEmpty()) return null;

        // Filter only unlocked topics
        List<Topic> unlockedTopics = topics.stream()
                .filter(Topic::isUnlocked)
                .collect(Collectors.toList());

        if (unlockedTopics.isEmpty()) {
            // Fallback to the first topic if nothing is unlocked yet
            return topics.get(0);
        }

        Topic[] topicArray = unlockedTopics.toArray(new Topic[0]);

        MinHeapTree heap = new MinHeapTree();
        heap.createMinHeapTree(topicArray);

        // The root of the min-heap contains the topic with the lowest confidence
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
        String currentCourse = nextCourse();

        if (isCorrect) {
            this.sessionScore++;
            //If the user got it right, remove it from missed history
            if (historyStore != null) {
                historyStore.removeMissed(student.getName(), currentCourse, questionId);
            }
        } else {
            //If the user got it wrong, add it to missed history
            if (historyStore != null) {
                historyStore.addMissed(student.getName(), currentCourse, questionId);
            }
        }

        //Always record it as a recently answered question
        if (historyStore != null) {
            historyStore.addRecent(student.getName(), currentCourse, questionId);
        }
    }

    @Override
    public void finishTurn(String course) {
        if (sessionTotalQuestions == 0) return;

        long elapsedTimeMs = System.currentTimeMillis() - turnStartTime;
        int timeSpentSeconds = (int) (elapsedTimeMs / 1000);

        if (topicStore != null) {
            Topic currentTopic = getCurrentTargetTopic();
            if (currentTopic != null && currentTopic.getCourse().equalsIgnoreCase(course)) {
                int nextLevel = difficultyPolicy.nextLevel(currentTopic.getLevel(), sessionScore, sessionTotalQuestions);
                currentTopic.setLevel(nextLevel);
                currentTopic.setLatestScore(sessionScore);
                if (sessionScore > currentTopic.getBestScore()) {
                    currentTopic.setBestScore(sessionScore);
                }

                //Unlock subsequent topics if score is >= 70
                if (courseGraphs != null && courseGraphs.containsKey(course)) {
                    Graph graph = courseGraphs.get(course);
                    if (graph != null && currentTopic.getLatestScore() >= 70) {
                        graph.unlockNext(currentTopic.getName());
                    }
                }

                topicStore.saveTopic(student.getName(), currentTopic);
            }
        }

        if (sessionStore != null) {
            Session sessionRecord = new Session(course, LocalDate.now(), sessionScore, timeSpentSeconds);
            sessionStore.saveSession(student.getName(), sessionRecord);
        }

        //Reset stats and rotate the queue for the next course
        this.sessionScore = 0;
        this.sessionTotalQuestions = 0;

        String finishedCourse = courseQueue.poll();
        courseQueue.offer(finishedCourse);

        this.turnStartTime = System.currentTimeMillis();
    }
}