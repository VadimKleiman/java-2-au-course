package ru.spbau.mit;

import java.util.function.Function;

public interface LightFuture<T> {
    boolean isReady();
    <R> LightFuture<R> thenApply(Function<T, R> task);
    void run();
    T get() throws LightExecutionException;
}
