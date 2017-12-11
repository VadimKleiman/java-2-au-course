package common;

import java.io.Serializable;

public class Tuple<T, R, S> implements Serializable {
    private T first;
    private R second;
    private S third;

    public Tuple(T first, R second, S third) {
        this.first = first;
        this.second = second;
        this.third = third;
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

    public S getThird() {
        return third;
    }

    public void setThird(S third) {
        this.third = third;
    }
}