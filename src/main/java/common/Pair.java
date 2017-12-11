package common;

import java.io.Serializable;
import java.util.Map;

public final class Pair<T, R> implements Map.Entry<T, R>, Serializable {
    private T key;
    private R value;

    public Pair(T key, R value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public T getKey() {
        return key;
    }

    @Override
    public R getValue() {
        return value;
    }

    @Override
    public R setValue(R value) {
        R old = this.value;
        this.value = value;
        return old;
    }

    public boolean equals(Object other) {
        if(this == other)
            return true;
        if(!(other instanceof Pair))
            return false;
        Pair that = (Pair)other;
        return (this.key == null ? that.key == null : this.key.equals(that.key)) &&
                (this.value == null ? that.value == null : this.value.equals(that.value));
    }

    public int hashCode() {
        return key.hashCode() * 3 + value.hashCode() * 5;
    }
}