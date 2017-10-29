package ru.spbau.mit;

import java.util.function.Function;

public interface LightFuture<T> {
    boolean isReady();
    <R> LightFuture<R> thenApply(Function<? super T, R> task);
    T get() throws LightExecutionException;
}
