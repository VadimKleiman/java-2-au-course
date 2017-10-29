package ru.spbau.mit;

import org.junit.Assert;
import org.junit.Test;
import java.util.ArrayList;
import java.util.Collections;

import static java.lang.Thread.sleep;

public class ThreadPoolImplTest {
    private final int threadNumber = Runtime.getRuntime().availableProcessors();
    @Test
    public void testCountThreads() throws InterruptedException {
        final int sleep = 100;
        int begin = Thread.activeCount();
        ThreadPoolImpl pool = new ThreadPoolImpl(threadNumber);
        sleep(sleep);
        int end = Thread.activeCount();
        Assert.assertEquals(end - begin, threadNumber);
        pool.shutdown();
    }

    @Test
    public void testShutdown() {
        int begin = Thread.activeCount();
        ThreadPoolImpl pool = new ThreadPoolImpl(threadNumber);
        pool.shutdown();
        int end = Thread.activeCount();
        Assert.assertEquals(end - begin, 0);
    }

    @Test
    public void testThenApply() {
        ThreadPoolImpl pool = new ThreadPoolImpl(threadNumber);
        final int resultVal = pool.submit(() -> {
            final int val = 10;
            return val + val;
        }).thenApply(result -> result * result).thenApply(result -> result * 2).get();
        final int checkVal = 800;
        Assert.assertEquals(resultVal, checkVal);
        pool.shutdown();
    }

    @Test
    public void testIsReady() {
        ThreadPoolImpl pool = new ThreadPoolImpl(threadNumber);
        LightFuture future = pool.submit(() -> {
            final int iterationCount = 1000000;
            int result = 0;
            for (int i = 0; i < iterationCount; i++) {
                result += i;
            }
            return result;
        });
        Assert.assertFalse(future.isReady());
        future.get();
        Assert.assertTrue(future.isReady());
        pool.shutdown();
    }

    @Test(expected = LightExecutionException.class)
    public void testException() {
        ThreadPoolImpl pool = new ThreadPoolImpl(threadNumber);
        pool.submit(null).get();
        pool.shutdown();
    }

    @Test
    public void testCorrectWorkers() {
        ThreadPoolImpl pool = new ThreadPoolImpl(threadNumber);
        ArrayList<LightFuture> future = new ArrayList<>();
        final int n = 100000;
        for (int i = 0; i < n; i++) {
            future.add(pool.submit(() -> {
                final int iterationCount = 1000;
                int result = 0;
                for (int it = 0; it < iterationCount; it++) {
                    result += it;
                }
                return result;
            }));
        }
        final int checkFirst = 499500;
        future.forEach(i -> Assert.assertEquals(i.get(), checkFirst));
        pool.shutdown();
    }

    @Test
    public void testSubmit() throws InterruptedException {
        ThreadPoolImpl pool = new ThreadPoolImpl(threadNumber);
        ArrayList<Thread> threads = new ArrayList<>();
        final ArrayList<Integer> result = new ArrayList<>();
        final int iterationCount = 200;
        for (int i = 0; i < iterationCount; i++) {
            final int exp = i;
            threads.add(new Thread(() -> {
                synchronized (result) {
                    result.add(pool.submit(() -> exp + exp).get());
                }
            }));
        }
        threads.forEach(Thread::start);
        for (int i = 0; i < iterationCount; i++) {
            threads.get(i).join();
        }
        Collections.sort(result);
        Assert.assertEquals(result.size(), iterationCount);
        for (int i = 0; i < iterationCount; i++) {
            int res = result.get(i);
            Assert.assertEquals(res, i + i);
        }
        pool.shutdown();
    }
}
