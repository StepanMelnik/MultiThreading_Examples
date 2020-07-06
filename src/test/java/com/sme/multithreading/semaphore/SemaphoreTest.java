package com.sme.multithreading.semaphore;

import static com.sme.multithreading.util.ThreadUtil.sleepInMilliSeconds;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The example demonstrates how {@link Semaphore} works.
 */
public class SemaphoreTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SemaphoreTest.class);

    private final List<String> result = new ArrayList<>();
    private final Lock lock = new ReentrantLock();

    private int count;

    /**
     * <pre>
     * Initialize {@link Semaphore} with 2 permits.
     * 
     * Start three threads in the following queue:
     * 1) thread1 with 500ms delay;
     * 2) thread2 with 700ms delay;
     * 3) thread3 without delay.
     * 
     * It means thread3 and thread1 should be the first in the queue.
     * The threads get permits in semaphore, because we initialized semaphore with 2 permits only. And thread3 will wait until some thread releases permit.
     * 
     * thread3 starts own logic when permit is allowed in semaphore.
     * The unit test asserts result from the latest thread only.
     * </pre>
     */
    @Test
    void testSemaphore() throws Exception
    {
        Semaphore semaphore = new Semaphore(2);
        LOGGER.debug("Available {} permits in {} thread", semaphore.availablePermits(), Thread.currentThread().getName());
        LOGGER.debug("{} queue length in {} thread", semaphore.getQueueLength(), Thread.currentThread().getName());
        LOGGER.debug("{} has queue thread in {} thread", semaphore.hasQueuedThreads(), Thread.currentThread().getName());

        Thread thread1 = new Thread(() ->
        {
            sleepInMilliSeconds(500, s -> LOGGER.error(s, Thread.currentThread().getName()), "{} thread is interrupted");

            LOGGER.debug("Available {} permits in {} thread", semaphore.availablePermits(), Thread.currentThread().getName());
            LOGGER.debug("{} queue length in {} thread", semaphore.getQueueLength(), Thread.currentThread().getName());
            LOGGER.debug("{} has queue thread in {} thread", semaphore.hasQueuedThreads(), Thread.currentThread().getName());

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
                    safeIncrement(step, Thread.currentThread().getName());
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
            LOGGER.debug("{} queue length in {} thread", semaphore.getQueueLength(), Thread.currentThread().getName());
            LOGGER.debug("{} has queue thread in {} thread", semaphore.hasQueuedThreads(), Thread.currentThread().getName());

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
                    safeIncrement(step, Thread.currentThread().getName());
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
            LOGGER.debug("{} queue length in {} thread", semaphore.getQueueLength(), Thread.currentThread().getName());
            LOGGER.debug("{} has queue thread in {} thread", semaphore.hasQueuedThreads(), Thread.currentThread().getName());

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
                    safeIncrement(step, Thread.currentThread().getName());
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

        LOGGER.debug("{} thread is latest in the queue. So, the tail of result should have values from the last result only", thread2.getName());
        assertEquals(IntStream.range(21, 31)
                .boxed()
                .map(i -> "thread2_" + i)
                .collect(toList()),
                result.stream()
                        .skip(20)
                        .collect(toList()),
                "Expects a proper result added in the \"thread2\" only");
    }

    private void safeIncrement(int step, String threadName)
    {
        try
        {
            lock.lock();
            count++;
            LOGGER.debug("{} thread in {} step with {} result", threadName, step, result);
            result.add(threadName + count);
        }
        finally
        {
            lock.unlock();
        }
    }
}
