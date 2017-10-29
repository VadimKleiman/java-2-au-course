package ru.spbau.mit;

import java.util.function.Supplier;

public interface ThreadPool {
    <T> LightFuture<T> submit(Supplier<T> task);
    void shutdown();
}
