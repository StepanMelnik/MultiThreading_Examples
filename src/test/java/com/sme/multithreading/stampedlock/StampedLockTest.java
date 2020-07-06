package com.sme.multithreading.stampedlock;

import static com.sme.multithreading.util.ThreadUtil.sleepInMilliSeconds;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.StampedLock;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sme.multithreading.synchronizedmethod.SynchronizedMethodTest;

/**
 * The test demonstrates how {@link StampedLock} works. Be careful to work with {@link StampedLock}, because StampedLock is not reentrant, hence it
 * went into a deadlock situation.
 */
public class StampedLockTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SynchronizedMethodTest.class);

    private final List<Integer> result = new ArrayList<>();
    private int count;

    @BeforeEach
    void setUp()
    {
        result.clear();
        count = 0;
    }

    @Test
    void testStampedLock() throws Exception
    {
        StampedLock stampedLock = new StampedLock();
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Runnable writeTask = () ->
        {
            long stamp = stampedLock.writeLock();
            try
            {
                increment(Thread.currentThread().getName());
            }
            finally
            {
                stampedLock.unlockWrite(stamp);
            }
        };

        Runnable readTask = () ->
        {
            long stamp = stampedLock.readLock();
            try
            {
                LOGGER.debug("Get {} value", getCount());
            }
            finally
            {
                stampedLock.unlockRead(stamp);
            }
        };

        executor.submit(writeTask);
        executor.submit(writeTask);
        executor.submit(writeTask);
        executor.submit(writeTask);
        executor.submit(writeTask);

        executor.submit(readTask);

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        assertEquals(asList(1, 2, 3, 4, 5), result);
    }

    @Test
    void testOptimisicRead() throws Exception
    {
        StampedLock stampedLock = new StampedLock();
        ExecutorService executor = Executors.newFixedThreadPool(2);

        Runnable writeTask = () ->
        {
            long stamp = stampedLock.writeLock();
            try
            {
                sleepInMilliSeconds(1000, s -> LOGGER.error(s, Thread.currentThread().getName()), "{} thread is interrupted");
                increment(Thread.currentThread().getName());
            }
            finally
            {
                stampedLock.unlock(stamp);
            }
        };

        Runnable readTask = () ->
        {
            long stamp = stampedLock.tryOptimisticRead();
            try
            {
                IntStream.range(1, 25).forEach(step ->
                {
                    LOGGER.debug("Validate optimistic lock = {}", stampedLock.validate(stamp));
                    sleepInMilliSeconds(100, s -> LOGGER.error(s, Thread.currentThread().getName()), "{} thread is interrupted");
                });

                LOGGER.debug("Get {} value", getCount());
            }
            finally
            {
                stampedLock.unlock(stamp);
            }
        };

        executor.submit(readTask);
        executor.submit(writeTask);
        executor.submit(writeTask);

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        assertEquals(asList(1, 2), result);
    }

    private void increment(String threadName)
    {
        count++;
        LOGGER.debug("{} thread incremented count value: {}", threadName, count);
        result.add(count);
    }

    private int getCount()
    {
        return count;
    }
}
