package lbycpds.group2.structures;

public class CircularQueue<T> implements QueueInterface<T>{
    private class Node<T> {
        T data;
        Node<T> next;

        Node(T data) {
            this.data = data;
        }
    }

    private Node<T> front;
    private Node<T> back;
    private int size;
    private int capacity;

    public CircularQueue(int capacity) {
        this.capacity = capacity;
        this.size = 0;
    }

    @Override
    public void offer(T item) {
        Node<T> freshNode = new Node<>(item);
        if (isEmpty()) {
            front = freshNode;
            back = freshNode;
            back.next = front;
        } else {
            back.next = freshNode;
            back = freshNode;
            back.next = front;
        }
        size++;

    }

    @Override
    public T poll() {
        if (isEmpty()) {
            return null;
        }

        T polled = front.data;
        front = front.next;
        size--;

        if (front == null) {
            back = null;
        } else {
            back.next = front;
        }

        return polled;
    }

    @Override
    public T front() {
        if (isEmpty()) {
            return null;
        }

        return front.data;
    }

    @Override
    public T back() {
        if (isEmpty()) {
            return null;
        }
        return back.data;
    }

    public T afterBack() {
        if (isEmpty()) {
            return null;
        }
        return back.next.data;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    void displayQueue() {
        if (isEmpty()) {
            System.out.println("Queue is empty.");
            return;
        }

        Node<T> temp = front;
        System.out.print("Queue contents (front to back): ");
        do {
            System.out.print(temp.data + " ");
            temp = temp.next;
        } while (temp != front);
        System.out.println();
    }


    @Override
    public boolean isFull() {
        return size >= capacity;
    }

    //Test
    public static void main(String[] args) {
        CircularQueue<String> queue = new CircularQueue<>(3);

        System.out.println("Initial State");
        System.out.println("Is empty? " + queue.isEmpty()); // true
        System.out.println("Is full? " + queue.isFull());   // false
        System.out.println();

        System.out.println("Adding 3 items");
        queue.offer("Item 1");
        queue.offer("Item 2");
        queue.offer("Item 3");

        queue.displayQueue(); // Should show: Item 1, Item 2, Item 3
        System.out.println("Size: " + queue.size());       // 3
        System.out.println("Is full? " + queue.isFull());   // true
        System.out.println("Front: " + queue.front());     // Item 1
        System.out.println("Back: " + queue.back());       // Item 3
        System.out.println("After Back (Loops to Front): " + queue.afterBack()); // Item 1
        System.out.println();

        System.out.println("Polling 2 items");
        System.out.println("Polled: " + queue.poll()); // Removes Item 1
        System.out.println("Polled: " + queue.poll()); // Removes Item 2

        queue.displayQueue(); // Should show: Item 3
        System.out.println("Size after reduction: " + queue.size()); // 1
        System.out.println("New Front: " + queue.front()); // Item 3
        System.out.println("New Back: " + queue.back());   // Item 3
        System.out.println("After Back: " + queue.afterBack()); // Item 3 (points to itself since it's the only node)
        System.out.println();


        System.out.println("Wrapping Around (Adding Item 4 and Item 5)");
        queue.offer("Item 4");
        queue.offer("Item 5");

        queue.displayQueue(); // Should show logical order: Item 3, Item 4, Item 5
        System.out.println("Size: " + queue.size());       // 3
        System.out.println("Is full again? " + queue.isFull()); // true
        System.out.println("Front: " + queue.front());     // Item 3
        System.out.println("Back: " + queue.back());       // Item 5

        System.out.println("After Back (Loops to Front): " + queue.afterBack()); // Item 3
        System.out.println();


        System.out.println("Emptying the queue completely");
        System.out.println("Polled: " + queue.poll()); // Item 3
        System.out.println("Polled: " + queue.poll()); // Item 4
        System.out.println("Polled: " + queue.poll()); // Item 5

        queue.displayQueue(); // Should show: Queue is empty.
        System.out.println("Is empty? " + queue.isEmpty()); // true
        System.out.println("Front is now: " + queue.front()); // null
        System.out.println("Back is now: " + queue.back());   // null
    }


}
