package com.sme.multithreading.reentrantlock;

import static com.sme.multithreading.util.ThreadUtil.sleepInMilliSeconds;
import static com.sme.multithreading.util.ThreadUtil.sleepInSeconds;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The unit test demonstrates how to work with conditional lock.
 */
public class ReentrantLockWithConditionTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ReentrantLockWithConditionTest.class);

    private int count;
    private final List<Integer> result = new ArrayList<>();

    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();

    /**
     * Create two threads.
     * <p>
     * <ul>
     * The following logic perfromed in the threads
     * <li>Run first thread and put in {@link Condition#await()} condition;</li>
     * <li>Run second thread and send signal to wake up condition;</li>
     * <li>Process all logic in the second thread in lock mode;</li>
     * <li>Process all logic in the first thread in lock mode.</li>
     * </ul>
     * </p>
     */
    @Test
    void testLockCondition() throws Exception
    {
        Thread thread1 = new Thread(() ->
        {
            lock.lock();
            try
            {
                LOGGER.debug("Put lock in await condition in {} thread", Thread.currentThread().getName());
                condition.await();   // wait a signal to wake up
            }
            catch (InterruptedException e)
            {
                LOGGER.error("The {} thread is interrupted", Thread.currentThread().getName());
            }

            try
            {
                LOGGER.debug("Processing first thread in the lock");
                IntStream.range(0, 10).forEach(step ->
                {
                    increment(step, Thread.currentThread().getName());
                    sleepInMilliSeconds(800, s -> LOGGER.error(s, Thread.currentThread().getName()), "{} thread is interrupted");
                });
            }
            finally
            {
                lock.unlock();
                LOGGER.debug("Unlock first thread");
            }
        });

        Thread thread2 = new Thread(() ->
        {
            lock.lock();
            sleepInSeconds(10, s -> LOGGER.error(s, Thread.currentThread().getName()), "{} thread is interrupted");

            LOGGER.debug("Send signal to wake up condition in {} thread", Thread.currentThread().getName());
            condition.signal(); // send signal to wake up

            try
            {
                LOGGER.debug("Processing second thread in the lock");
                IntStream.range(0, 20).forEach(step ->
                {
                    increment(step, Thread.currentThread().getName());
                    sleepInMilliSeconds(500, s -> LOGGER.error(s, Thread.currentThread().getName()), "{} thread is interrupted");
                });
            }
            finally
            {
                lock.unlock();
                LOGGER.debug("Unlock second thread");
            }
        });

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        LOGGER.debug("The result should have ordered integer [1..30] range");
        assertEquals(IntStream.range(1, 31).boxed().collect(Collectors.toList()), result);
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
}
