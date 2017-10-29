package ru.spbau.mit;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ThreadPoolImpl implements ThreadPool {
    private final ArrayDeque<Runnable> deque = new ArrayDeque<>();
    private final ArrayList<Thread> workers = new ArrayList<>();
    private volatile boolean isShutdown = false;

    ThreadPoolImpl(int nThread) {
        for (int i = 0; i < nThread; i++) {
            workers.add(new Thread(new Worker()));
        }
        workers.forEach(Thread::start);
    }
    @Override
    public <T> LightFuture<T> submit(Supplier<T> task) {
        if (isShutdown) {
            throw new IllegalStateException("ThreadPool can't submit new task");
        }
        LightFutureImpl<T> future = new LightFutureImpl<>(task);
        synchronized (deque) {
            deque.add(future::run);
            deque.notifyAll();
        }
        return future;
    }

    @Override
    public void shutdown() {
        isShutdown = true;
        workers.forEach(Thread::interrupt);
        workers.forEach(w -> {
            try {
                w.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    private final class Worker implements Runnable {
        @Override
        public void run() {
            try {
                Runnable task;
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

    private final class LightFutureImpl<T> implements LightFuture<T> {
        private Supplier<? extends T> task = null;
        private T result = null;
        private Throwable error = null;
        private final ArrayList<LightFutureImpl<?>> arrayFunc = new ArrayList<>();
        private volatile boolean isDone = false;

        LightFutureImpl(Supplier<? extends T> task) {
            this.task = task;
        }

        @Override
        public boolean isReady() {
            return isDone;
        }

        @Override
        public <R> LightFuture<R> thenApply(Function<? super T, R> func) {
            Supplier<R> task = () -> func.apply(get());
            synchronized (arrayFunc) {
                if (isDone) {
                    return submit(task);
                } else {
                    LightFutureImpl<R> out = new LightFutureImpl<>(task);
                    arrayFunc.add(out);
                    return out;
                }
            }
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
                throw new LightExecutionException("Interrupt", e);
            }
            if (error != null) {
                throw new LightExecutionException("Function exited with an error ", error);
            }
            return result;
        }

        private synchronized void run() {
            try {
                result = task.get();
                isDone = true;
                synchronized (arrayFunc) {
                    arrayFunc.forEach(LightFutureImpl::run);
                    arrayFunc.clear();
                }
            } catch (Throwable e) {
                error = e;
            } finally {
                isDone = true;
                notifyAll();
            }
        }
    }
}
