package ru.spbau.mit;

public interface LockFreeList<T> {
    boolean isEmpty();

    void append(T value);

    boolean remove(T value);

    boolean contains(T value);
}