package com.sme.multithreading.atomic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The test shows how to work with counter variable using synchronized method and {@link AtomicInteger}.
 */
public class TharedSafeCounterTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(TharedSafeCounterTest.class);

    @Test
    void testCounterInSynchronizedBlock() throws InterruptedException
    {
        SafeCounter safeCounter = new SafeCounter();

        Thread thread1 = new Thread(() ->
        {
            IntStream.range(0, 10).forEach(step ->
            {
                try
                {
                    int counter = safeCounter.increment();

                    LOGGER.debug("{} thread1 in {} step with {} count result", Thread.currentThread().getName(), step, counter);

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
                try
                {
                    int counter = safeCounter.increment();

                    LOGGER.debug("{} thread2 in {} step with {} count result", Thread.currentThread().getName(), step, counter);

                    TimeUnit.MILLISECONDS.sleep(600);
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
        assertEquals(IntStream.range(1, 31).boxed().collect(Collectors.toList()), safeCounter.getResult());
    }

    @Test
    void testCounterByAtomicInteger() throws Exception
    {
        AtomicSafeCounter safeCounter = new AtomicSafeCounter();

        Thread thread1 = new Thread(() ->
        {
            IntStream.range(0, 10).forEach(step ->
            {
                try
                {
                    int counter = safeCounter.increment();

                    LOGGER.debug("{} thread1 in {} step with {} count result", Thread.currentThread().getName(), step, counter);

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
                try
                {
                    int counter = safeCounter.increment();

                    LOGGER.debug("{} thread2 in {} step with {} count result", Thread.currentThread().getName(), step, counter);

                    TimeUnit.MILLISECONDS.sleep(600);
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
        assertEquals(IntStream.range(0, 30).boxed().collect(Collectors.toList()), safeCounter.getResult());
    }

    /**
     * The thread safe counter that uses synchronized {@link this#increment()} method to increment counter variable.
     */
    private static class SafeCounter
    {
        private final List<Integer> result = new ArrayList<>();
        private int counter;

        /**
         * Get list of created counters.
         * 
         * @return Returns counter list.
         */
        List<Integer> getResult()
        {
            return result;
        }

        /**
         * Increment variable.
         */
        synchronized int increment()
        {
            counter++;
            result.add(counter);
            return counter;
        }
    }

    /**
     * The thread safe counter that uses {@link AtomicInteger} to increment counter variable.
     */
    private static class AtomicSafeCounter
    {
        private final List<Integer> result = new ArrayList<>();
        private final AtomicInteger counter = new AtomicInteger();

        /**
         * Get list of created counters.
         * 
         * @return Returns counter list.
         */
        List<Integer> getResult()
        {
            return result;
        }

        /**
         * Increment variable. No synchronized method here!
         */
        int increment()
        {
            /*
            while (true)
            {
                int oldValue = counter.get();
                int newValue = oldValue + 1;
                if (counter.compareAndSet(oldValue, newValue))
                {
                    result.add(counter.get());
                    return counter.get();
                }
            }
            */

            int value = counter.getAndIncrement();
            result.add(value);
            return value;
        }
    }
}
