package com.sme.multithreading.countdown;

import static com.sme.multithreading.util.ThreadUtil.sleepInSeconds;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit tests to show how {@link CountDownLatch} works.
 */
public class CountDownLatchTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CountDownLatchTest.class);

    /**
     * The test creates 3 threads and controls all of them in CountDownLatch instance to wait each other.
     */
    @Test
    void testCountDown() throws InterruptedException
    {
        final CountDownLatch countDownLatch = new CountDownLatch(3);

        Thread thread1 = new Thread(() ->
        {
            sleepInSeconds(10, s -> LOGGER.error(s, Thread.currentThread().getName()), "{} thread is interrupted");

            LOGGER.debug("Thread 1 ({}) thread is UP", Thread.currentThread().getName());
            LOGGER.debug("CountDownLatch#count() = {} in {} thread", countDownLatch.getCount(), Thread.currentThread().getName());
            countDownLatch.countDown();
        });

        Thread thread2 = new Thread(() ->
        {
            sleepInSeconds(5, s -> LOGGER.error(s, Thread.currentThread().getName()), "{} thread is interrupted");

            LOGGER.debug("Thread 2 ({}) thread is UP", Thread.currentThread().getName());
            LOGGER.debug("CountDownLatch#count() = {} in {} thread", countDownLatch.getCount(), Thread.currentThread().getName());
            countDownLatch.countDown();
        });

        Thread thread3 = new Thread(() ->
        {
            sleepInSeconds(7, s -> LOGGER.error(s, Thread.currentThread().getName()), "{} thread is interrupted");

            LOGGER.debug("Thread 3 ({}) thread is UP", Thread.currentThread().getName());
            LOGGER.debug("CountDownLatch#count() = {} in {} thread", countDownLatch.getCount(), Thread.currentThread().getName());
            countDownLatch.countDown();
        });

        thread1.start();
        thread2.start();
        thread3.start();

        // no thread joins here

        assertTrue(thread1.isAlive(), "Expects alive thread1");
        assertTrue(thread2.isAlive(), "Expects alive thread2");
        assertTrue(thread3.isAlive(), "Expects alive thread3");

        countDownLatch.await();

        assertFalse(thread1.isAlive(), "Expects dead thread1");
        assertFalse(thread2.isAlive(), "Expects dead thread2");
        assertFalse(thread3.isAlive(), "Expects dead thread3");
    }
}
