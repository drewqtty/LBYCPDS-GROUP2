package lbycpds.group2.test;

import lbycpds.group2.model.Topic;
import lbycpds.group2.structures.CircularQueue;
import lbycpds.group2.structures.DataStructure;
import lbycpds.group2.structures.Graph;
import lbycpds.group2.structures.MinHeapTree;
import lbycpds.group2.structures.ProgressionGraph;
import lbycpds.group2.structures.QueueInterface;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DataStructuresFixesTest {

    private static int passed = 0;
    private static int failed = 0;


    private static void section(String title, String problem) {
        System.out.println();
        System.out.println("==================================================================");
        System.out.println(title);
        System.out.println("  Problem: " + problem);
        System.out.println("==================================================================");
    }

    private static void info(String text) {
        System.out.println("  . " + text);
    }

    private static void check(String what, Object expected, Object actual) {
        boolean ok = expected == null ? actual == null : expected.equals(actual);
        record(ok, what, "expected: " + expected, "actual:   " + actual);
    }

    private static void checkTrue(String what, boolean condition, String detail) {
        record(condition, what, "expected: condition to hold", "actual:   " + detail);
    }

    private static void expectThrows(String what, Class<? extends Throwable> type, Runnable action) {
        String actual;
        boolean ok;
        try {
            action.run();
            actual = "no exception thrown";
            ok = false;
        } catch (Throwable t) {
            actual = t.getClass().getSimpleName() + (t.getMessage() != null ? " (\"" + t.getMessage() + "\")" : "");
            ok = type.isInstance(t);
        }
        record(ok, what, "expected: " + type.getSimpleName(), "actual:   " + actual);
    }

    private static void record(boolean ok, String what, String expectedLine, String actualLine) {
        if (ok) {
            passed++;
        } else {
            failed++;
        }
        System.out.println("  [" + (ok ? "PASS" : "FAIL") + "] " + what);
        System.out.println("         " + expectedLine);
        System.out.println("         " + actualLine);
    }

    private static Topic topic(String name, int confidence, int latest, boolean unlocked) {
        Topic t = new Topic(name, "C");
        t.setConfidence(confidence);
        t.setLatestScore(latest);
        t.setBestScore(latest);     // Graph decides "completed" from the best score
        t.setUnlocked(unlocked);
        return t;
    }

    private static List<String> names(List<Topic> topics) {
        List<String> out = new ArrayList<>();
        for (Topic t : topics) {
            out.add(t.getName());
        }
        return out;
    }

    private static Object readField(Object target, String fieldName) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        return f.get(target);
    }

    // Number of outgoing edges stored for a topic
    @SuppressWarnings("unchecked")
    private static int edgeCount(Graph graph, Topic from) throws Exception {
        Map<Topic, LinkedList<Topic>> adjacency = (Map<Topic, LinkedList<Topic>>) readField(graph, "topics");
        return adjacency.get(from).size();
    }


    public static void main(String[] args) throws Exception {
        System.out.println("STRUCTURES FIXES TEST");
        System.out.println("Each section covers one change to CircularQueue, Graph, MinHeapTree or the interfaces.");

        fix1_queueEnforcesCapacity();
        fix2_queueRejectsBadInput();
        fix3_pollReleasesTheLastNode();
        fix4_graphIgnoresDuplicateTopics();
        fix5_graphValidatesEdges();
        fix6_unlockNextReportsOnlyNewlyUnlocked();
        fix7_unlockRootsOnInterfaceInStableOrder();
        fix8_heapDoesNotTouchCallerArray();
        fix9_heapTiesAreDeterministic();
        fix10_interfacesAndNodeClass();

        System.out.println();
        System.out.println("==================================================================");
        System.out.println("SUMMARY: " + passed + " passed, " + failed + " failed, " + (passed + failed) + " checks");
        System.out.println("==================================================================");
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void fix1_queueEnforcesCapacity() {
        section("FIX 1  CircularQueue: offer() enforces the capacity",
                "offer() never checked isFull(), so a queue of capacity 3 silently grew to any size.");

        CircularQueue<String> queue = new CircularQueue<>(3);
        queue.offer("Item 1");
        queue.offer("Item 2");
        queue.offer("Item 3");
        info("capacity 3, three items offered");
        check("size", 3, queue.size());
        check("queue reports full", true, queue.isFull());

        expectThrows("a fourth offer is rejected", IllegalStateException.class, () -> queue.offer("Item 4"));
        check("size is unchanged after the rejected offer", 3, queue.size());
        check("back is still the third item", "Item 3", queue.back());

        info("regression: after a poll there is room again and the queue wraps around");
        check("polled", "Item 1", queue.poll());
        queue.offer("Item 4");
        check("front", "Item 2", queue.front());
        check("back", "Item 4", queue.back());
        check("after back loops to front", "Item 2", queue.afterBack());
    }

    private static void fix2_queueRejectsBadInput() {
        section("FIX 2  CircularQueue: constructor and offer() validate their input",
                "A capacity of 0 or less was accepted (isFull() was then always true) and null items were "
                        + "allowed, so poll() returning null could not tell \"empty\" from \"a null item\".");

        expectThrows("capacity 0 is rejected", IllegalArgumentException.class, () -> new CircularQueue<String>(0));
        expectThrows("negative capacity is rejected", IllegalArgumentException.class,
                () -> new CircularQueue<String>(-5));

        CircularQueue<String> queue = new CircularQueue<>(2);
        expectThrows("offering null is rejected", NullPointerException.class, () -> queue.offer(null));
        check("the rejected null was not counted", 0, queue.size());
        check("queue is still empty", true, queue.isEmpty());

        info("regression: capacity 1 is the smallest valid queue");
        CircularQueue<String> one = new CircularQueue<>(1);
        one.offer("only");
        check("single-slot queue is full", true, one.isFull());
        check("single-slot queue polls its item", "only", one.poll());
    }

    private static void fix3_pollReleasesTheLastNode() throws Exception {
        section("FIX 3  CircularQueue: poll() releases the last node",
                "After the last item was polled, front and back still pointed at the removed node (the "
                        + "front == null branch could never run), so the old data stayed referenced.");

        CircularQueue<String> queue = new CircularQueue<>(3);
        queue.offer("A");
        queue.offer("B");
        info("two items offered, then both polled");
        check("first poll", "A", queue.poll());
        check("second poll", "B", queue.poll());

        check("size", 0, queue.size());
        check("internal front reference was cleared", null, readField(queue, "front"));
        check("internal back reference was cleared", null, readField(queue, "back"));
        check("front() on an empty queue", null, queue.front());
        check("back() on an empty queue", null, queue.back());
        check("afterBack() on an empty queue", null, queue.afterBack());
        check("poll() on an empty queue", null, queue.poll());

        info("regression: the queue is fully usable again after being emptied");
        queue.offer("C");
        queue.offer("D");
        check("front", "C", queue.front());
        check("back", "D", queue.back());
        check("afterBack loops to front", "C", queue.afterBack());
        check("size", 2, queue.size());
    }

    private static void fix4_graphIgnoresDuplicateTopics() throws Exception {
        section("FIX 4  Graph: addTopic() ignores duplicates",
                "Adding a topic whose name was already in the graph replaced its edge lists, wiping every "
                        + "edge that had been added and leaving the old topic orphaned.");

        Graph graph = new Graph();
        Topic a = topic("A", 1, 90, true);
        Topic b = topic("B", 1, 0, false);
        graph.addTopic(a);
        graph.addTopic(b);
        graph.addEdge("A", "B");
        info("A -> B was added");
        check("edges stored for A", 1, edgeCount(graph, a));

        graph.addTopic(a);
        info("addTopic(A) was called again with the same object");
        check("edges stored for A are kept", 1, edgeCount(graph, a));

        graph.addTopic(topic("A", 9, 0, false));
        info("addTopic was called again with a different Topic named \"A\"");
        check("edges stored for A are still kept", 1, edgeCount(graph, a));
        check("size does not count the duplicates", 2, graph.size());

        check("unlockNext still works through the kept edge", "[B]", names(graph.unlockNext("A")).toString());
    }

    private static void fix5_graphValidatesEdges() throws Exception {
        section("FIX 5  Graph: addEdge() validates its input",
                "An edge to or from an unknown topic was silently dropped (a typo in a topic file went "
                        + "unnoticed), and self-loops and duplicate edges were stored.");

        Graph graph = new Graph();
        Topic a = topic("A", 1, 0, false);
        Topic b = topic("B", 1, 0, false);
        graph.addTopic(a);
        graph.addTopic(b);

        expectThrows("unknown target topic", IllegalArgumentException.class, () -> graph.addEdge("A", "Typo"));
        expectThrows("unknown source topic", IllegalArgumentException.class, () -> graph.addEdge("Typo", "B"));
        check("no edge was stored by the failed calls", 0, edgeCount(graph, a));

        graph.addEdge("A", "A");
        info("addEdge(\"A\", \"A\") was called");
        check("self-loop was not stored", 0, edgeCount(graph, a));

        graph.addEdge("A", "B");
        graph.addEdge("A", "B");
        info("addEdge(\"A\", \"B\") was called twice");
        check("duplicate edge was not stored twice", 1, edgeCount(graph, a));
    }

    private static void fix6_unlockNextReportsOnlyNewlyUnlocked() {
        section("FIX 6  Graph: unlockNext() reports only topics that were newly unlocked",
                "It added every reachable topic to the result, including topics that were already unlocked, "
                        + "so callers saw old topics as \"just opened\".");

        Graph graph = new Graph();
        Topic a = topic("A", 1, 90, true);
        Topic b = topic("B", 1, 90, true);    // already unlocked and passed
        Topic c = topic("C", 1, 0, false);    // locked, A is its only prerequisite
        Topic d = topic("D", 1, 0, false);    // locked, only reachable through B
        Topic e = topic("E", 1, 0, false);    // locked, also needs F which is not passed
        Topic f = topic("F", 1, 10, false);
        for (Topic t : Arrays.asList(a, b, c, d, e, f)) {
            graph.addTopic(t);
        }
        graph.addEdge("A", "B");
        graph.addEdge("A", "C");
        graph.addEdge("A", "E");
        graph.addEdge("F", "E");
        graph.addEdge("B", "D");

        info("A(90) -> B(90, already open), C, E     F(10) -> E     B -> D   (scores are best scores)");
        List<Topic> opened = graph.unlockNext("A");
        info("unlockNext(\"A\") returned " + names(opened));

        check("only newly unlocked topics are listed (B was already open)", "[C, D]", names(opened).toString());
        check("C is now unlocked", true, c.isUnlocked());
        check("D is unlocked by going through the already-open B", true, d.isUnlocked());
        check("E stays locked because F was not passed", false, e.isUnlocked());

        info("regression: calling it again finds nothing new");
        check("second call result", "[]", names(graph.unlockNext("A")).toString());
        check("unknown topic name", "[]", names(graph.unlockNext("Nope")).toString());
    }

    private static void fix7_unlockRootsOnInterfaceInStableOrder() {
        section("FIX 7  ProgressionGraph / Graph: unlockRoots() is on the interface and keeps insertion order",
                "unlockRoots() was only reachable through the concrete Graph, and its HashMap returned the "
                        + "roots in a different order from run to run.");

        ProgressionGraph graph = new Graph();     // typed as the interface on purpose
        Topic r3 = topic("Root 3", 1, 0, false);
        Topic r1 = topic("Root 1", 1, 0, false);
        Topic child = topic("Child", 1, 0, false);
        Topic r2 = topic("Root 2", 1, 0, false);
        for (Topic t : Arrays.asList(r3, r1, child, r2)) {
            graph.addTopic(t);
        }
        graph.addEdge("Root 3", "Child");

        List<Topic> roots = graph.unlockRoots();
        info("topics were added in the order: Root 3, Root 1, Child, Root 2");
        check("roots come back in insertion order", "[Root 3, Root 1, Root 2]", names(roots).toString());
        check("Root 3 is unlocked", true, r3.isUnlocked());
        check("Root 1 is unlocked", true, r1.isUnlocked());
        check("Root 2 is unlocked", true, r2.isUnlocked());
        check("the child topic stays locked", false, child.isUnlocked());

        check("empty graph has no roots", "[]", names(new Graph().unlockRoots()).toString());
    }

    private static void fix8_heapDoesNotTouchCallerArray() throws Exception {
        section("FIX 8  MinHeapTree: createMinHeapTree() copies the array and accepts null",
                "The caller's array was reordered in place, and a null array crashed with a NullPointerException.");

        Topic[] topics = {
                topic("Stack", 5, 0, true),
                topic("BST", 2, 0, true),
                topic("Graph", 1, 0, true),
                topic("Queue", 3, 0, true),
                topic("Lists", 4, 0, true)
        };
        MinHeapTree heap = new MinHeapTree();
        heap.createMinHeapTree(topics);

        info("confidence: Stack 5, BST 2, Graph 1, Queue 3, Lists 4");
        check("root is the weakest topic", "Graph", heap.root.data.getName());
        check("the caller's array keeps its original order", "[Stack, BST, Graph, Queue, Lists]",
                names(Arrays.asList(topics)).toString());

        MinHeapTree empty = new MinHeapTree();
        empty.createMinHeapTree(new Topic[0]);
        check("empty array gives an empty tree", true, empty.root == null);

        MinHeapTree fromNull = new MinHeapTree();
        fromNull.createMinHeapTree(topics);
        fromNull.createMinHeapTree(null);
        info("createMinHeapTree(null) was called on a tree that already had a root");
        check("null array clears the tree without an exception", true, fromNull.root == null);

        info("regression: heap order holds for every parent and child");
        checkTrue("each child is not weaker than its parent", heapOrderHolds(heap.root), "heap order violated");
    }

    // left/right are package-private in TreeNode, so they are read by reflection
    private static boolean heapOrderHolds(MinHeapTree.TreeNode node) throws Exception {
        if (node == null) {
            return true;
        }
        MinHeapTree.TreeNode left = (MinHeapTree.TreeNode) readField(node, "left");
        MinHeapTree.TreeNode right = (MinHeapTree.TreeNode) readField(node, "right");
        if (left != null && left.data.compareTo(node.data) < 0) {
            return false;
        }
        if (right != null && right.data.compareTo(node.data) < 0) {
            return false;
        }
        return heapOrderHolds(left) && heapOrderHolds(right);
    }

    private static void fix9_heapTiesAreDeterministic() {
        section("FIX 9  Topic.compareTo / MinHeapTree: ties pick the same topic every time",
                "Two topics with the same confidence and latest score compared as equal, so which one was "
                        + "chosen as the weakest depended on the order they were given in.");

        Topic x = topic("Alpha", 3, 40, true);
        Topic y = topic("Beta", 3, 40, true);

        MinHeapTree forward = new MinHeapTree();
        forward.createMinHeapTree(new Topic[]{x, y});
        MinHeapTree backward = new MinHeapTree();
        backward.createMinHeapTree(new Topic[]{y, x});

        info("Alpha and Beta both have confidence 3 and latest score 40");
        check("root when given [Alpha, Beta]", "Alpha", forward.root.data.getName());
        check("root when given [Beta, Alpha]", "Alpha", backward.root.data.getName());

        info("regression: confidence and score still decide first");
        Topic weaker = topic("Zulu", 1, 0, true);
        MinHeapTree mixed = new MinHeapTree();
        mixed.createMinHeapTree(new Topic[]{x, y, weaker});
        check("lower confidence wins over a better name", "Zulu", mixed.root.data.getName());
        check("lower score wins when confidence is equal", true,
                topic("Z", 3, 10, true).compareTo(topic("A", 3, 20, true)) < 0);
    }

    private static void fix10_interfacesAndNodeClass() {
        section("FIX 10  Interfaces and node classes: shared DataStructure contract, static nested nodes",
                "QueueInterface repeated DataStructure's methods without extending it, and the node classes were "
                        + "non-static inner classes that carried a hidden reference to their outer object.");

        check("QueueInterface extends DataStructure", true, DataStructure.class.isAssignableFrom(QueueInterface.class));
        check("a CircularQueue can be used as a DataStructure", true, new CircularQueue<String>(1) instanceof DataStructure);
        check("a Graph can be used as a DataStructure", true, new Graph() instanceof DataStructure);

        check("MinHeapTree.TreeNode is a static nested class", true,
                Modifier.isStatic(MinHeapTree.TreeNode.class.getModifiers()));

        boolean nodeIsStatic = false;
        for (Class<?> inner : CircularQueue.class.getDeclaredClasses()) {
            if (inner.getSimpleName().equals("Node")) {
                nodeIsStatic = Modifier.isStatic(inner.getModifiers());
            }
        }
        check("CircularQueue.Node is a static nested class", true, nodeIsStatic);
    }
}