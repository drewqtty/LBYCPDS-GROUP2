package lbycpds.group2.structures;

import lbycpds.group2.model.Topic;

import java.util.*;

public class Graph implements ProgressionGraph {
    private HashMap<Topic, LinkedList<Topic>> topics = new HashMap<>();

    // used for finding topic objects by their string name
    private HashMap<String, Topic> nameToTopicMap = new HashMap<>();

    private HashMap<Topic, LinkedList<Topic>> prerequisites = new HashMap<>();

    @Override
    public void addTopic(Topic topic) {
        topics.put(topic, new LinkedList<>());
        prerequisites.put(topic, new LinkedList<>());
        nameToTopicMap.put(topic.getName(), topic);
    }

    @Override
    public void addEdge(String fromTopic, String toTopic) {
        Topic from = nameToTopicMap.get(fromTopic);
        Topic to = nameToTopicMap.get(toTopic);

        if (from != null && to != null) {
            // from -> to direction
            topics.get(from).add(to);
            prerequisites.get(from).add(to);
        }
    }

    @Override
    public List<Topic> unlockNext(String topicName) {
        List<Topic> unlocked = new LinkedList<>();
        Topic startTopic = nameToTopicMap.get(topicName);

        if (startTopic == null) {
            return  unlocked;
        }

        //placeholder capacity
        CircularQueue<Topic> q = new CircularQueue<>(50);
        Set<Topic> visited = new HashSet<>();

        q.offer(startTopic);
        visited.add(startTopic);


        while (!q.isEmpty()) {
            Topic current = q.poll();

            for (Topic neighbor: topics.getOrDefault(current, new LinkedList<>())) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);

                    if (areAllPrerequisitesCompleted(neighbor)) {
                        neighbor.setUnlocked(true);
                        unlocked.add(neighbor);
                        q.offer(neighbor);
                    }
                }
            }
        }
        return unlocked;

    }

    private boolean areAllPrerequisitesCompleted(Topic topic) {
        LinkedList<Topic> prereqs = prerequisites.get(topic);

        //if a topic has no prerequisites, it's ready to unlock
        if (prereqs == null || prereqs.isEmpty()) {
            return true;
        }

        //check if every prerequisite meets your completion criteria
        for (Topic prereq : prereqs) {
            if (!isTopicCompleted(prereq)) {
                return false;
            }
        }

        return true;
    }



    private boolean isTopicCompleted(Topic topic) {
        //placeholder score, will come back once the scoring algorithm is completed
        return topic.getLatestScore() >= 70;
    }

    @Override
    public boolean isEmpty() {
        return nameToTopicMap.isEmpty();
    }

    @Override
    public int size() {
        return nameToTopicMap.size();
    }


    //test
    public static void main(String[] args) {
        ProgressionGraph graph = new Graph();


        Topic sp = new Topic("Superposition", "FUNDCK1"); // Completed (>= 70) 85
        Topic dw = new Topic("Delta to Wye", "FUNDCK1"); // Not completed (< 70) 60
        Topic mesh = new Topic("Mesh Analysis", "FUNDCK1"); // Locked/Unrated 0
        Topic ac = new Topic("AC sources", "FUNDCK1"); // Locked/Unrated 0

        sp.setLatestScore(85);
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

        //test unlocking next topics starting from "Superposition"
        System.out.println("Unlocking from Superposition");
        List<Topic> unlockedTopics = graph.unlockNext("Superposition");

        System.out.println("Unlocked topics count: " + unlockedTopics.size());
        for (Topic t : unlockedTopics) {
            System.out.println(" - Unlocked: " + t.getName() + " (Score: " + t.getLatestScore() + ")");
        }

        // Find and display topics that are still locked
        System.out.println("\nLocked topics:");
        List<Topic> allTopics = List.of(sp, dw, mesh, ac);
        for (Topic t : allTopics) {
            if (!unlockedTopics.contains(t) && t.getLatestScore() < 70 && !t.getName().equals("Superposition")) {
                System.out.println(" - Locked: " + t.getName() + " (Prerequisite not met or score below 70)");
            }
        }


    }

}
