package com.sme.multithreading.synchronizedmethod;

import static com.sme.multithreading.util.ThreadUtil.sleepInMilliSeconds;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit tests shows how to increment not volatile field in syncronized method in multi-threading system.
 */
public class SynchronizedMethodTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SynchronizedMethodTest.class);

    private final List<Integer> result = new ArrayList<>();
    private int count;

    /**
     * The test creates two threads with different delays in runnable object.
     * <p>
     * Two threads call not synchronized method to increase the same value. As a result we should get a result with not ordered [1..30] range.
     * </p>
     */
    @Test
    void testNotSynchronizedMethod() throws InterruptedException
    {
        Thread thread1 = new Thread(() ->
        {
            IntStream.range(0, 10).forEach(step ->
            {
                increment(step, Thread.currentThread().getName());
                try
                {
                    TimeUnit.MILLISECONDS.sleep(1000);
                }
                catch (InterruptedException e)
                {
                    LOGGER.error("{} thread is interrupted", Thread.currentThread().getName(), e);
                }
            });
        });

        Thread thread2 = new Thread(() ->
        {
            IntStream.range(0, 20).forEach(step ->
            {
                increment(step, Thread.currentThread().getName());
                try
                {
                    TimeUnit.MILLISECONDS.sleep(500);
                }
                catch (InterruptedException e)
                {
                    LOGGER.error("{} thread is interrupted", Thread.currentThread().getName(), e);
                }
            });
        });

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        LOGGER.debug("The result should have not ordered integer [1..30] range");
        // no assertion here
    }

    /**
     * The test creates two threads with different delays in runnable object.
     * <p>
     * Two threads call synchronized method to increase the same value. As a result we should get a proper result with [1..30] range.
     * </p>
     */
    @Test
    void testSynchronizedMethod() throws InterruptedException
    {
        Thread thread1 = new Thread(() ->
        {
            IntStream.range(0, 10).forEach(step ->
            {
                synchronizedIncrement(step, Thread.currentThread().getName());
                sleepInMilliSeconds(1000, s -> LOGGER.error(s, Thread.currentThread().getName()), "{} thread is interrupted");
            });
        });

        Thread thread2 = new Thread(() ->
        {
            IntStream.range(0, 20).forEach(step ->
            {
                synchronizedIncrement(step, Thread.currentThread().getName());
                sleepInMilliSeconds(500, s -> LOGGER.error(s, Thread.currentThread().getName()), "{} thread is interrupted");
            });
        });

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        LOGGER.debug("The result should have ordered integer [1..30] range");
        assertEquals(IntStream.range(1, 31).boxed().collect(Collectors.toList()), result);
    }

    private synchronized void synchronizedIncrement(int step, String threadName)
    {
        count++;
        LOGGER.debug("{} thread in {} step with {} count result", threadName, step, count);
        result.add(count);
    }

    private void increment(int step, String threadName)
    {
        count++;
        LOGGER.debug("{} thread in {} step with {} count result", threadName, step, count);
        result.add(count);
    }
}
