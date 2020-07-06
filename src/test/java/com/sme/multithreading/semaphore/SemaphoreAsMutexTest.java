package com.sme.multithreading.semaphore;

import static com.sme.multithreading.util.ThreadUtil.sleepInMilliSeconds;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The test demonstrates how to implement Mutex using {@link Semaphor}. Mutex is the Semaphore with an access count of 1.
 */
public class SemaphoreAsMutexTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SemaphoreAsMutexTest.class);

    private final List<String> result = new ArrayList<>();
    private int count;

    @Test
    void testMutex() throws Exception
    {
        Semaphore semaphore = new Semaphore(1);
        LOGGER.debug("Available {} permits in {} thread", semaphore.availablePermits(), Thread.currentThread().getName());

        Thread thread1 = new Thread(() ->
        {
            sleepInMilliSeconds(500, s -> LOGGER.error(s, Thread.currentThread().getName()), "{} thread is interrupted");

            LOGGER.debug("Available {} permits in {} thread", semaphore.availablePermits(), Thread.currentThread().getName());

            try
            {
                semaphore.acquire();
                LOGGER.debug("Got the permit in {} thread", Thread.currentThread().getName());
            }
            catch (InterruptedException e)
            {
                LOGGER.error("Semaphore is interrupted in {} thread", Thread.currentThread().getName(), e);
            }

            try
            {
                IntStream.range(0, 10).forEach(step ->
                {
                    increment(step, Thread.currentThread().getName());
                    sleepInMilliSeconds(1000, s -> LOGGER.error(s, Thread.currentThread().getName()), "{} thread is interrupted");
                });
            }
            finally
            {
                LOGGER.debug("Release locking in {} thread", Thread.currentThread().getName());
                semaphore.release();
                LOGGER.debug("Available {} permits in {} thread", semaphore.availablePermits(), Thread.currentThread().getName());
            }
        }, "thread1_");

        Thread thread2 = new Thread(() ->
        {
            sleepInMilliSeconds(700, s -> LOGGER.error(s, Thread.currentThread().getName()), "{} thread is interrupted");

            LOGGER.debug("Available {} permits in {} thread", semaphore.availablePermits(), Thread.currentThread().getName());

            try
            {
                semaphore.acquire();
                LOGGER.debug("Got the permit in {} thread", Thread.currentThread().getName());
            }
            catch (InterruptedException e)
            {
                LOGGER.error("Semaphore is interrupted in {} thread", Thread.currentThread().getName(), e);
            }

            try
            {
                IntStream.range(0, 10).forEach(step ->
                {
                    increment(step, Thread.currentThread().getName());
                    sleepInMilliSeconds(1000, s -> LOGGER.error(s, Thread.currentThread().getName()), "{} thread is interrupted");
                });
            }
            finally
            {
                LOGGER.debug("Release locking in {} thread", Thread.currentThread().getName());
                semaphore.release();
                LOGGER.debug("Available {} permits in {} thread", semaphore.availablePermits(), Thread.currentThread().getName());
            }
        }, "thread2_");

        Thread thread3 = new Thread(() ->
        {
            LOGGER.debug("Available {} permits in {} thread", semaphore.availablePermits(), Thread.currentThread().getName());

            try
            {
                semaphore.acquire();
                LOGGER.debug("Got the permit in {} thread", Thread.currentThread().getName());
            }
            catch (InterruptedException e)
            {
                LOGGER.error("Semaphore is interrupted in {} thread", Thread.currentThread().getName(), e);
            }

            try
            {
                IntStream.range(0, 10).forEach(step ->
                {
                    increment(step, Thread.currentThread().getName());
                    sleepInMilliSeconds(1000, s -> LOGGER.error(s, Thread.currentThread().getName()), "{} thread is interrupted");
                });
            }
            finally
            {
                LOGGER.debug("Release locking in {} thread", Thread.currentThread().getName());
                semaphore.release();
                LOGGER.debug("Available {} permits in {} thread", semaphore.availablePermits(), Thread.currentThread().getName());
            }
        }, "thread3_");

        thread1.start();
        thread2.start();
        thread3.start();

        thread1.join();
        thread2.join();
        thread3.join();

        List<String> expectedResult = new ArrayList<>();
        expectedResult.addAll(IntStream.range(1, 11)
                .boxed()
                .map(i -> "thread3_" + i)
                .collect(toList()));
        expectedResult.addAll(IntStream.range(11, 21)
                .boxed()
                .map(i -> "thread1_" + i)
                .collect(toList()));
        expectedResult.addAll(IntStream.range(21, 31)
                .boxed()
                .map(i -> "thread2_" + i)
                .collect(toList()));

        assertEquals(expectedResult, result);
    }

    /**
     * The method is not synchronized, because Semaphore works with queue of one permit.
     */
    private void increment(int step, String threadName)
    {
        count++;
        LOGGER.debug("{} thread in {} step with {} result", threadName, step, result);
        result.add(threadName + count);
    }
}
