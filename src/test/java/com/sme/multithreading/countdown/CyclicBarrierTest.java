package com.sme.multithreading.countdown;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit tests to show how {@link CyclicBarrier} works.
 */
public class CyclicBarrierTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CyclicBarrierTest.class);

    /**
     * Create 3 threads and synchronize them in barrier to all wait for each other to reach a common barrier point.
     */
    @Test
    void testCyclicBarrier() throws Exception
    {
        AtomicBoolean atomicBoolean = new AtomicBoolean();

        CyclicBarrier cyclicBarrier = new CyclicBarrier(3, () ->
        {
            LOGGER.debug("All thread reaches barrier");
            atomicBoolean.compareAndSet(false, true);
        });

        Thread thread1 = new Thread(() ->
        {
            try
            {
                TimeUnit.SECONDS.sleep(2);
                LOGGER.debug("Thread 1 ({}) thread is UP", Thread.currentThread().getName());

                LOGGER.debug("CyclicBarrier#getNumberWaiting() = {} in {} thread", cyclicBarrier.getNumberWaiting(), Thread.currentThread().getName());
                cyclicBarrier.await();

                LOGGER.debug("Thread 1 ({}) thread continue to work", Thread.currentThread().getName());
            }
            catch (InterruptedException | BrokenBarrierException e)
            {
                LOGGER.debug("{} thread is interrupted", Thread.currentThread().getName());
            }
        });

        Thread thread2 = new Thread(() ->
        {
            try
            {
                TimeUnit.SECONDS.sleep(12);
                LOGGER.debug("Thread 2 ({}) thread is UP", Thread.currentThread().getName());

                LOGGER.debug("CyclicBarrier#getNumberWaiting() = {} in {} thread", cyclicBarrier.getNumberWaiting(), Thread.currentThread().getName());
                cyclicBarrier.await();

                LOGGER.debug("Thread 2 ({}) thread continue to work", Thread.currentThread().getName());
            }
            catch (InterruptedException | BrokenBarrierException e)
            {
                LOGGER.debug("{} thread is interrupted", Thread.currentThread().getName());
            }
        });

        Thread thread3 = new Thread(() ->
        {
            try
            {
                TimeUnit.SECONDS.sleep(8);
                LOGGER.debug("Thread 3 ({}) thread is UP", Thread.currentThread().getName());

                LOGGER.debug("CyclicBarrier#getNumberWaiting() = {} in {} thread", cyclicBarrier.getNumberWaiting(), Thread.currentThread().getName());
                cyclicBarrier.await();

                LOGGER.debug("Thread 3 ({}) thread continue to work", Thread.currentThread().getName());
            }
            catch (InterruptedException | BrokenBarrierException e)
            {
                LOGGER.debug("{} thread is interrupted", Thread.currentThread().getName());
            }
        });

        thread1.start();
        thread2.start();
        thread3.start();

        assertFalse(atomicBoolean.get(), "Expects false as initialized value");

        thread1.join();
        thread2.join();
        thread3.join();

        assertTrue(atomicBoolean.get(), "Expects true, because all thread reaches barrier");
    }
}
