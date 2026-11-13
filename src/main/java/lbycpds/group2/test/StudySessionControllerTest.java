package lbycpds.group2.test;

import lbycpds.group2.StudySessionController;
import lbycpds.group2.logic.*;
import lbycpds.group2.model.*;
import lbycpds.group2.structures.Graph;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class StudySessionControllerTest {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("STUDY SESSION CONTROLLER TEST");
        System.out.println("==================================================\n");


        MockTopicStore topicStore = new MockTopicStore();
        MockSessionStore sessionStore = new MockSessionStore();
        MockQuestionHistoryStore historyStore = new MockQuestionHistoryStore();


        DifficultyPolicy difficultyPolicy = (currentLevel, score, total) -> {
            double percentage = total > 0 ? (double) score / total : 0.0;
            if (percentage >= 0.5) {
                System.out.println("   [Adaptive Engine] Performance (" + (int)(percentage * 100) + "%) passed threshold! Level increased: " + currentLevel + " -> " + (currentLevel + 1));
                return currentLevel + 1;
            } else {
                System.out.println("   [Adaptive Engine] Performance (" + (int)(percentage * 100) + "%) below threshold. Level remains: " + currentLevel);
                return currentLevel;
            }
        };

        StudySessionController controller = new StudySessionController(
                topicStore, sessionStore, historyStore, difficultyPolicy
        );


        Student student = new Student("Charlie");
        student.addCourse("DataStructures");
        student.addCourse("Algorithms");
        student.addCourse("Databases");


        Topic t1 = new Topic("ArraysAndStrings", "DataStructures"); t1.setUnlocked(true); t1.setConfidence(10);
        Topic t2 = new Topic("LinkedLists", "DataStructures"); t2.setUnlocked(true); t2.setConfidence(25);
        Topic t3 = new Topic("BinarySearchTrees", "DataStructures"); t3.setUnlocked(false); t3.setConfidence(5);
        topicStore.setTopicsForCourse("Charlie", "DataStructures", Arrays.asList(t1, t2, t3));

        Topic t4 = new Topic("SortingAlgorithms", "Algorithms"); t4.setUnlocked(true); t4.setConfidence(15);
        Topic t5 = new Topic("GraphTraversal", "Algorithms"); t5.setUnlocked(true); t5.setConfidence(30);
        topicStore.setTopicsForCourse("Charlie", "Algorithms", Arrays.asList(t4, t5));

        Topic t6 = new Topic("BPlusTrees", "Databases"); t6.setUnlocked(true); t6.setConfidence(20);
        topicStore.setTopicsForCourse("Charlie", "Databases", Arrays.asList(t6));

        // Define a collection of questions to test
        Question q1 = new IdentificationQuestion(1, "DataStructures", "ArraysAndStrings", 1,
                "What is the average time complexity of accessing an element in an array by index?", "O(1)");

        Question q2 = new MultipleChoiceQuestion(2, "DataStructures", "LinkedLists", 1,
                "Which pointer points to the end of a singly linked list?", "Null", new String[]{"Head", "Null", "Tail", "Root"});

        Question q3 = new IdentificationQuestion(3, "Algorithms", "SortingAlgorithms", 2,
                "What sorting algorithm uses a divide-and-conquer pivoting strategy with average O(n log n)?", "Quicksort");


        System.out.println("--- [SCENARIO 1: Normal Single-Course Session] ---");
        SingleCourseStudySession normalSession = (SingleCourseStudySession) controller.createSession(
                StudyMode.NORMAL, student, "DataStructures", new Graph(), null, null
        );

        System.out.println("Target Course: " + normalSession.nextCourse());
        Topic targetTopic = normalSession.getTargetTopic();
        System.out.println("Selected Weakest Topic (via MinHeap): " + (targetTopic != null ? targetTopic.getName() : "None") + " (Level: " + targetTopic.getLevel() + ")");


        System.out.println("\n-> Answering Q1 (Correctly): 'O(1)'");
        normalSession.recordAnswer(q1, "O(1)");

        System.out.println("-> Answering Q2 (Incorrectly): 'Tail'");
        normalSession.recordAnswer(q2, "Tail");

        System.out.println("\n[History Check] Missed questions for DataStructures after Q2 failure: " + historyStore.loadMissed("Charlie", "DataStructures"));

        System.out.println("\n-> Finishing Turn for 'DataStructures'...");
        normalSession.finishTurn("DataStructures");
        System.out.println("Session completed and progress saved.\n");

        System.out.println("--- [SCENARIO 2: Cram Mode with Circular Queue Rotation] ---");
        Map<String, Graph> courseGraphs = new HashMap<>();
        CramStudySession cramSession = controller.createCramSession(student, courseGraphs);

        for (int i = 1; i <= 3; i++) {
            String activeCourse = cramSession.nextCourse();
            System.out.println("\n[Cram Turn " + i + "] Current Course in Rotation: " + activeCourse);

            Topic cramTarget = cramSession.getCurrentTargetTopic();
            System.out.println("   - Target Topic: " + (cramTarget != null ? cramTarget.getName() : "None"));


            System.out.println("   - Answering Q3 correctly...");
            cramSession.recordAnswer(q3, "Quicksort");

            System.out.println("   - Finishing turn for: " + activeCourse);
            cramSession.finishTurn(activeCourse);
        }

        System.out.println("\n--- [SCENARIO 3: Reviewing Missed Questions] ---");
        System.out.println("Missed question IDs currently stored for DataStructures: " + historyStore.loadMissed("Charlie", "DataStructures"));

        MissedQuestionsStudySession missedSession = (MissedQuestionsStudySession) controller.createSession(
                StudyMode.MISSED, student, "DataStructures", null, null, null
        );

        System.out.println("Starting missed questions review session for: " + missedSession.nextCourse());
        System.out.println("-> Retrying Q2 correctly with answer: 'Null'");
        missedSession.recordAnswer(q2, "Null");

        missedSession.finishTurn("DataStructures");
        System.out.println("[History Check] Remaining missed questions after correct review: " + historyStore.loadMissed("Charlie", "DataStructures"));

        System.out.println("\n==================================================");
        System.out.println("ALL PROCESS TRACES COMPLETED SUCCESSFULLY");
        System.out.println("==================================================");
    }
}