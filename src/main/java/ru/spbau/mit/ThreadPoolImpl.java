package ru.spbau.mit;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Supplier;

public class ThreadPoolImpl implements ThreadPool {
    private final ArrayDeque<LightFuture> deque = new ArrayDeque<>();
    private final ArrayList<Thread> workers = new ArrayList<>();

    ThreadPoolImpl(int nThread) {
        for (int i = 0; i < nThread; i++) {
            workers.add(new Thread(new Worker()));
        }
        workers.forEach(Thread::start);
    }
    @Override
    public <T> LightFuture<T> submit(Supplier<T> task) {
        LightFutureImpl<T> future = new LightFutureImpl<>(task);
        synchronized (deque) {
            deque.add(future);
            deque.notifyAll();
        }
        return future;
    }

    @Override
    public void shutdown() {
        workers.forEach(Thread::interrupt);
        workers.forEach(w -> {
            try {
                w.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    private class Worker implements Runnable {
        @Override
        public void run() {
            try {
                LightFuture task;
                while (!Thread.interrupted()) {
                    synchronized (deque) {
                        while (deque.isEmpty()) {
                            deque.wait();
                        }
                        task = deque.removeFirst();
                    }
                    task.run();
                }
            } catch (InterruptedException ignored) {
            }
        }
    }

    public class LightFutureImpl<T> implements LightFuture<T> {
        private volatile boolean isDone = false;
        private Supplier<? extends T> task = null;
        private T result = null;
        private Throwable error = null;

        LightFutureImpl(Supplier<? extends T> task) {
            this.task = task;
        }

        @Override
        public boolean isReady() {
            return isDone;
        }

        @Override
        public <R> LightFuture<R> thenApply(Function<T, R> func) {
            Supplier<R> task = () -> func.apply(get());
            return submit(task);
        }

        @Override
        public T get() {
            try {
                synchronized (this) {
                    while (!isDone) {
                        wait();
                    }
                }
            } catch (InterruptedException e) {
                throw new LightExecutionException();
            }
            if (error != null) {
                throw new LightExecutionException();
            }
            return result;
        }

        @Override
        public synchronized void run() {
            try {
                result = task.get();
            } catch (Throwable e) {
                error = e;
            } finally {
                isDone = true;
                notifyAll();
            }
        }
    }
}
