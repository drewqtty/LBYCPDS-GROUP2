package lbycpds.group2.structures;

public interface QueueStructure<T> extends DataStructure {
    void enqueue(T item);

    T dequeue();
}