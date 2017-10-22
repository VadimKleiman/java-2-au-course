package ru.spbau.mit;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicMarkableReference;

class LockFreeListImpl<T> implements LockFreeList<T> {
    @Override
    public boolean isEmpty() {
        return size.get() == 0;
    }

    @Override
    public void append(T value) {
        Node newNode = new Node(value);
        while(true) {
            final Tuple result = find(null, true);
            if (result.curr.next.compareAndSet(null, newNode, false, false)) {
                size.incrementAndGet();
                break;
            }
        }
    }

    @Override
    public boolean remove(T value) {
        while(true) {
            final Tuple result = find(value, false);
            if (result.curr == null) {
                return false;
            }
            if (!result.curr.next.attemptMark(result.next, true)) {
                continue;
            }
            size.decrementAndGet();
            result.prev.next.compareAndSet(result.curr, result.next, false, false);
            return true;
        }
    }

    @Override
    public boolean contains(T value) {
        return find(value, false).curr != null;
    }

    private Tuple find(T value, boolean findLast) {
        Node prev, curr, next;
        final boolean[] mark = new boolean[1];
        retry:
        while(true) {
            prev = head.getReference();
            while(true) {
                curr = prev.next.getReference();
                if (findLast && curr == null) {
                    return new Tuple(null, prev, null);
                } else if (!findLast && curr == null) {
                    return new Tuple(null, null, null);
                }
                next = curr.next.get(mark);
                if (mark[0]) {
                    if (!prev.next.compareAndSet(curr, next, false, false)) {
                        continue retry;
                    }
                } else {
                    if (!findLast && curr.value.equals(value)) {
                        return new Tuple(prev, curr, next);
                    }
                    prev = curr;
                }
            }
        }
    }

    private final class Node {
        T value;
        AtomicMarkableReference<Node> next;

        Node(T value) {
            this.value = value;
            next = new AtomicMarkableReference<>(null, false);
        }

        Node() {
            next = new AtomicMarkableReference<>(null, false);
        }
    }

    private final class Tuple {
        Node prev;
        Node curr;
        Node next;

        Tuple(Node p, Node c, Node s) {
            prev = p;
            curr = c;
            next = s;
        }
    }

    private final AtomicInteger size = new AtomicInteger();
    private final AtomicMarkableReference<Node> head = new AtomicMarkableReference<>(new Node(), false);
}
