package com.sme.multithreading.threadinterrupt;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit tests to work with different cases how thread interrupting works.
 */
public class ThreadInterruptTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadInterruptTest.class);

    /**
     * A thread uses {@link Thread#sleep} methods that is interrupted by unit tests.
     * <p>
     * {@link InterruptedException} error occurs in the thread only when the thread is interrupted.
     * </p>
     */
    @Test
    void testInterruptWithCatchedException()
    {
        Thread thread = new Thread(() ->
        {
            LOGGER.debug("Start {} thread", Thread.currentThread().getName());

            try
            {
                TimeUnit.SECONDS.sleep(10);
            }
            catch (InterruptedException e)
            {
                LOGGER.error("The {} thread is interrupted. Make cleanup.", Thread.currentThread().getName(), e);
            }

            LOGGER.debug("Finish {} thread", Thread.currentThread().getName());
        });

        thread.start();

        assertTrue(thread.isAlive(), "Expects started thread");
        assertFalse(thread.isInterrupted(), "Expects not interrupted thread");

        thread.interrupt();

        assertTrue(thread.isAlive(), "Expects started thread");
        assertTrue(thread.isInterrupted(), "Expects interrupted thread");

        try
        {
            thread.join();
        }
        catch (InterruptedException e)
        {
            LOGGER.error("Main thread is interrupted", e);
        }
    }

    /**
     * Unit test sends a signal to interrupt thread. But in general nothing happens.
     * <p>
     * To test the issue by hand just enable/disable a commented section to sleep the thread.
     * </p>
     */
    @Test
    void testInterrupt()
    {
        Thread thread = new Thread(() ->
        {
            LOGGER.debug("Start {} thread", Thread.currentThread().getName());

            IntStream.range(1, Integer.MAX_VALUE / 10000).forEach(step ->
            {
                /*                try
                {
                    TimeUnit.MILLISECONDS.sleep(10);
                }
                catch (InterruptedException e)
                {
                    LOGGER.error("The {} thread is interrupted. Make cleanup.", Thread.currentThread().getName(), e);
                }
                */
                LOGGER.debug("Perform {} step in {} thread", step, Thread.currentThread().getName());
            });

            LOGGER.debug("Finish {} thread", Thread.currentThread().getName());
        });

        thread.start();
        thread.interrupt();

        assertTrue(thread.isAlive(), "Expects started thread");
        assertTrue(thread.isInterrupted(), "Expects interrupted thread");

        try
        {
            thread.join();
        }
        catch (InterruptedException e)
        {
            LOGGER.error("Main thread is interrupted", e);
        }
    }
}
