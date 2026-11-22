package lbycpds.group2.structures;

import lbycpds.group2.logic.StudyFlow;
import lbycpds.group2.model.Topic;

import java.util.*;

public class Graph implements ProgressionGraph {
    private LinkedHashMap<Topic, LinkedList<Topic>> topics = new LinkedHashMap<>();

    // used for finding topic objects by their string name
    private LinkedHashMap<String, Topic> nameToTopicMap = new LinkedHashMap<>();

    private LinkedHashMap<Topic, LinkedList<Topic>> prerequisites = new LinkedHashMap<>();

    @Override
    public void addTopic(Topic topic) {
        if (nameToTopicMap.containsKey(topic.getName())) return; // stops the method from creating a duplicate topic
        topics.put(topic, new LinkedList<>());
        prerequisites.put(topic, new LinkedList<>());
        nameToTopicMap.put(topic.getName(), topic);
    }

    @Override
    public void addEdge(String fromTopic, String toTopic) {
        Topic from = nameToTopicMap.get(fromTopic);
        Topic to = nameToTopicMap.get(toTopic);

        if (from == null || to == null) {
            throw new IllegalArgumentException(
                    "Unknown topic in edge: " + fromTopic + " -> " + toTopic);
        }
        if (from == to || topics.get(from).contains(to)) return;  // no self-loops or duplicates

        topics.get(from).add(to);
        prerequisites.get(to).add(from);
    }

    @Override
    public List<Topic> unlockNext(String topicName) {
        List<Topic> unlocked = new LinkedList<>();
        Topic startTopic = nameToTopicMap.get(topicName);

        if (startTopic == null) {
            return  unlocked;
        }


        CircularQueue<Topic> q = new CircularQueue<>(Math.max(1, topics.size()));
        Set<Topic> visited = new HashSet<>();

        q.offer(startTopic);
        visited.add(startTopic);


        while (!q.isEmpty()) {
            Topic current = q.poll();

            for (Topic neighbor: topics.getOrDefault(current, new LinkedList<>())) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);

                    if (areAllPrerequisitesCompleted(neighbor)) {
                        if (!neighbor.isUnlocked()) {
                            neighbor.setUnlocked(true);
                            unlocked.add(neighbor);
                        }
                        q.offer(neighbor);
                    }
                }
            }
        }
        return unlocked;

    }

    private boolean areAllPrerequisitesCompleted(Topic topic) {
        LinkedList<Topic> prereqs = prerequisites.get(topic);


        if (prereqs == null || prereqs.isEmpty()) {
            return true;
        }


        for (Topic prereq : prereqs) {
            if (!isTopicCompleted(prereq)) {
                return false;
            }
        }

        return true;
    }



    private boolean isTopicCompleted(Topic topic) {

        return topic.getBestScore() >= StudyFlow.PASS_PERCENT;
    }

    @Override
    public List<Topic> unlockRoots() {
        List<Topic> roots = new LinkedList<>();
        for (Topic topic : prerequisites.keySet()) {
            if (prerequisites.get(topic).isEmpty()) {
                topic.setUnlocked(true);
                roots.add(topic);
            }
        }
        return roots;
    }

    @Override
    public boolean isEmpty() {
        return nameToTopicMap.isEmpty();
    }

    @Override
    public int size() {
        return nameToTopicMap.size();
    }



    public static void main(String[] args) {
        ProgressionGraph graph = new Graph();


        Topic sp = new Topic("Superposition", "FUNDCK1"); // Completed (at or above the pass mark) 95
        Topic dw = new Topic("Delta to Wye", "FUNDCK1"); // Not completed (below the pass mark) 60
        Topic mesh = new Topic("Mesh Analysis", "FUNDCK1"); // Locked/Unrated 0
        Topic ac = new Topic("AC sources", "FUNDCK1"); // Locked/Unrated 0

        sp.setLatestScore(95);
        dw.setLatestScore(60);
        mesh.setLatestScore(0);
        ac.setLatestScore(0);

        //add topics to the graph
        graph.addTopic(sp);
        graph.addTopic(dw);
        graph.addTopic(mesh);
        graph.addTopic(ac);

        //add edges
        graph.addEdge("Superposition", "Delta to Wye");
        graph.addEdge("Superposition", "Mesh Analysis");
        graph.addEdge("Mesh Analysis", "AC sources");

        System.out.println("Graph Status");
        System.out.println("Is graph empty? " + graph.isEmpty());
        System.out.println("Total topics: " + graph.size());
        System.out.println();

        //display all topics and their scores
        System.out.println("Topics and Scores:");
        Topic[] allTopicsArray = {sp, dw, mesh, ac};
        for (Topic t : allTopicsArray) {
            System.out.println(" - " + t.getName() + " (Score: " + t.getLatestScore() + ")");
        }
        System.out.println();

        //display graph edges
        System.out.println("Graph Edges (Prerequisites):");
        System.out.println(" - Superposition -> Delta to Wye");
        System.out.println(" - Superposition -> Mesh Analysis");
        System.out.println(" - Mesh Analysis -> AC sources");
        System.out.println();



        System.out.println("Unlocking from Superposition");
        List<Topic> unlockedTopics = graph.unlockNext("Superposition");

        System.out.println("Unlocked topics count: " + unlockedTopics.size());
        for (Topic t : unlockedTopics) {
            System.out.println(" - Unlocked: " + t.getName() + " (Score: " + t.getLatestScore() + ")");
        }


        System.out.println("\nLocked topics:");
        List<Topic> allTopics = List.of(sp, dw, mesh, ac);
        for (Topic t : allTopics) {
            if (!unlockedTopics.contains(t) && t.getLatestScore() < StudyFlow.PASS_PERCENT && !t.getName().equals("Superposition")) {
                System.out.println(" - Locked: " + t.getName() + " (Prerequisite not met or score below " + StudyFlow.PASS_PERCENT + ")");
            }
        }


    }

}