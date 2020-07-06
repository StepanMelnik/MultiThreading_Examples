package com.sme.multithreading.sharedvariable;

import static com.sme.multithreading.util.ThreadUtil.sleepInMilliSeconds;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The test demonstrates how volatile increment variable works in the model memory.
 * 
 * <pre>
 * Volatile variables share the visibility features of synchronized, but none of the atomicity features.
 * So volatile alone is not strong enough to implement a counter.
 * 
 * volatile is useful to stop threads as described in VolatileVariableTest.
 * Another example described in https://en.wikipedia.org/wiki/Singleton_pattern#Lazy_initialization.
 * </pre>
 */
public class VolatileIncrementVariableTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(VolatileIncrementVariableTest.class);

    private final List<Integer> result = new ArrayList<>();
    private final Lock lock = new ReentrantLock();

    private volatile int count;

    @Test
    void testWrongLocalVariableCopyInStack() throws Exception
    {
        Thread thread1 = new Thread(() ->
        {
            IntStream.range(0, 20).forEach(step ->
            {
                notSafeIncrement(step, Thread.currentThread().getName());
                sleepInMilliSeconds(100, s -> LOGGER.error(s, Thread.currentThread().getName()), "{} thread is interrupted");
            });
        });

        Thread thread2 = new Thread(() ->
        {
            IntStream.range(0, 10).forEach(step ->
            {
                notSafeIncrement(step, Thread.currentThread().getName());
                sleepInMilliSeconds(200, s -> LOGGER.error(s, Thread.currentThread().getName()), "{} thread is interrupted");
            });
        });

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        LOGGER.debug("The result should have ordered integer [1..30] range");
        assertNotEquals(IntStream.range(1, 31).boxed().collect(Collectors.toList()), result, "Expects not equal result");
    }

    @Test
    void testSafeLocalVariableCopyInStack() throws Exception
    {
        Thread thread1 = new Thread(() ->
        {
            IntStream.range(0, 20).forEach(step ->
            {
                safeIncrement(step, Thread.currentThread().getName());
                sleepInMilliSeconds(100, s -> LOGGER.error(s, Thread.currentThread().getName()), "{} thread is interrupted");
            });
        });

        Thread thread2 = new Thread(() ->
        {
            IntStream.range(0, 10).forEach(step ->
            {
                safeIncrement(step, Thread.currentThread().getName());
                sleepInMilliSeconds(200, s -> LOGGER.error(s, Thread.currentThread().getName()), "{} thread is interrupted");
            });
        });

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        LOGGER.debug("The result should have ordered integer [1..30] range");
        assertEquals(IntStream.range(1, 31).boxed().collect(Collectors.toList()), result, "Expects qual result");
    }

    private void notSafeIncrement(int step, String threadName)
    {
        count++;
        LOGGER.debug("{} thread in {} step with {} count result", threadName, step, count);
        result.add(count);  // This is a bottle neck in test, because of the race operation
    }

    private void safeIncrement(int step, String threadName)
    {
        try
        {
            lock.lock();
            count++;
            LOGGER.debug("{} thread in {} step with {} count result", threadName, step, count);
            result.add(count);
        }
        finally
        {
            lock.unlock();
        }
        // this is another implementation
        //        synchronized (this)
        //        {
        //            count++;
        //            LOGGER.debug("{} thread in {} step with {} count result", threadName, step, count);
        //            result.add(count);
        //        }
    }
}
