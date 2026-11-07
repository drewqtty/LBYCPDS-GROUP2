package lbycpds.group2.structures;
import lbycpds.group2.model.Topic;

public class MinHeapTree {

    class TreeNode {
        TreeNode left;
        TreeNode right;
        Topic data;

        TreeNode() {
            left = null;
            right = null;
        }

        TreeNode(Topic data) {
            left = null;
            right = null;
            this.data = data;
        }
    }

    TreeNode root;

    public MinHeapTree() {
        root = null;
    }

    boolean isEmpty() {
        return root == null;
    }


    void createMinHeapTree(Topic[] topics) {
        int n = topics.length;

        //Apply min-heapify from the last non-leaf node up to the root
        for (int i = (n / 2) - 1; i >= 0; i--) {
            minHeapify(topics, n, i);
        }

        //Build the binary tree structure from the sorted heap array
        root = buildTree(topics, 0);
    }


    void minHeapify(Topic[] topics, int n, int i) {
        int smallest = i;
        int left = 2 * i + 1;
        int right = 2 * i + 2;

        //Compare priorities (Min-Heap: smallest confidence value comes to the top)
        if (left < n && topics[left].getConfidence() < topics[smallest].getConfidence()) {
            smallest = left;
        }

        if (right < n && topics[right].getConfidence() < topics[smallest].getConfidence()) {
            smallest = right;
        }

        if (smallest != i) {
            //Swap the Topic objects directly in the array
            Topic temp = topics[i];
            topics[i] = topics[smallest];
            topics[smallest] = temp;

            minHeapify(topics, n, smallest);
        }
    }

    TreeNode buildTree(Topic[] topics, int index) {
        if (index >= topics.length) {
            return null;
        }

        TreeNode newNode = new TreeNode(topics[index]);
        newNode.left = buildTree(topics, 2 * index + 1);
        newNode.right = buildTree(topics, 2 * index + 2);
        return newNode;
    }

    void displayTree(TreeNode node, String prefix, boolean isLeft) {
        if (node == null) {
            return;
        }


        displayTree(node.right, prefix + (isLeft ? "│   " : "    "), false);


        System.out.print(prefix);
        System.out.print(isLeft ? "└── " : "┌── ");
        System.out.println(node.data.getName());


        displayTree(node.left, prefix + (isLeft ? "    " : "│   "), true);
    }

    // method used for calling it in the main node
    void displayTree(TreeNode root) {
        displayTree(root, "", true);
    }

    //Test
    public static void main(String[] args) {
        MinHeapTree quiz = new MinHeapTree();

        Topic[] topics = {
                new Topic("Stack", "Data Structures and Algorithms"),
                new Topic("Binary Search Tree", "Data Structures and Algorithms"),
                new Topic("Graph", "Data Structures and Algorithms"),
                new Topic("Queue", "Data Structures and Algorithms"),
                new Topic("Linked Lists", "Data Structures and Algorithms")
        };

        topics[0].setConfidence(5);
        topics[1].setConfidence(2);
        topics[2].setConfidence(1);
        topics[3].setConfidence(3);
        topics[4].setConfidence(4);

        System.out.println("Topics and their confidence levels:");
        for (Topic t : topics) {
            System.out.println(" - " + t.getName() + " (Confidence: " + t.getConfidence() + ")");
        }
        System.out.println();

        quiz.createMinHeapTree(topics);

        System.out.println("Min-Heap tree structure:");
        quiz.displayTree(quiz.root);
        System.out.println();


    }
}
