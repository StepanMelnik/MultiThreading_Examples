package com.sme.multithreading.reentrantlock;

import static com.sme.multithreading.util.ThreadUtil.sleepInMilliSeconds;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The unit test demonstrates how to work with {@link ReentrantLock#lock} and {@link ReentrantLock#tryLock(long, TimeUnit)}.
 */
public class ReentrantLockTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ReentrantLockTest.class);

    private final Lock lock = new ReentrantLock();
    private final List<Integer> result = new ArrayList<>();
    private final List<Integer> tryInResult = new ArrayList<>();

    private int count;
    private int tryInCount;

    /**
     * The test creates two threads with different delays in runnable object.
     * <p>
     * Two threads call increment methods to increase a value using {@link ReentrantLock}.
     * </p>
     */
    @Test
    void testReentrantLock() throws Exception
    {
        Thread thread1 = new Thread(() ->
        {
            IntStream.range(0, 10).forEach(step ->
            {
                increment(step, Thread.currentThread().getName());
                tryToIncrement(step, Thread.currentThread().getName());
                sleepInMilliSeconds(1000, s -> LOGGER.error(s, Thread.currentThread().getName()), "{} thread is interrupted");
            });
        });

        Thread thread2 = new Thread(() ->
        {
            IntStream.range(0, 20).forEach(step ->
            {
                increment(step, Thread.currentThread().getName());
                tryToIncrement(step, Thread.currentThread().getName());
                sleepInMilliSeconds(500, s -> LOGGER.error(s, Thread.currentThread().getName()), "{} thread is interrupted");
            });
        });

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        LOGGER.debug("The result should have ordered integer [1..30] range");
        assertEquals(IntStream.range(1, 31).boxed().collect(Collectors.toList()), result);
        assertEquals(IntStream.range(1, 31).boxed().collect(Collectors.toList()), tryInResult);
    }

    private void increment(int step, String threadName)
    {
        lock.lock();
        try
        {
            count++;
            LOGGER.debug("{} thread in {} step with {} count result", threadName, step, count);
            result.add(count);
        }
        finally
        {
            lock.unlock();
        }
    }

    private void tryToIncrement(int step, String threadName)
    {
        try
        {
            boolean isLockAcquired = lock.tryLock(1, TimeUnit.SECONDS);

            if (isLockAcquired)
            {
                try
                {
                    tryInCount++;
                    LOGGER.debug("{} thread in {} step with {} count result", threadName, step, tryInCount);
                    tryInResult.add(count);
                }
                finally
                {
                    lock.unlock();
                }
            }

        }
        catch (InterruptedException e)
        {
            LOGGER.debug("Try lock is interrupted", e);
        }
    }
}
