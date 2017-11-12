package ru.spbau.mit;

public final class Tuple<T,R>
{
    private T first;
    private R second;

    public Tuple(T first, R second)
    {
        this.first = first;
        this.second = second;
    }

    public T getFirst() {
        return first;
    }

    public void setFirst(T first) {
        this.first = first;
    }

    public R getSecond() {
        return second;
    }

    public void setSecond(R second) {
        this.second = second;
    }
}
