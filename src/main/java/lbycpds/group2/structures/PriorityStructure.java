package lbycpds.group2.structures;

public interface PriorityStructure<T> extends DataStructure {
    void insert(T item);

    T extractFirst();

    T peek();
}