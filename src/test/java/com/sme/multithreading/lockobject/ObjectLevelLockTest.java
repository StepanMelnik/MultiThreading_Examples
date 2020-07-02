package com.sme.multithreading.lockobject;

import static com.sme.multithreading.util.ThreadUtil.sleepInMilliSeconds;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The test demonstrates how to work with Object level lock:
 * <li>synchronized method</li>
 * <li>synchronized block in method</li>
 * <li>synchronized object lock in method</li>
 */
public class ObjectLevelLockTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectLevelLockTest.class);

    @Test
    void testSynchrnoziedMethod() throws Exception
    {
        SynchrnoziedMethodSafeCounter methodSynchrnoziedSafeCounter = new SynchrnoziedMethodSafeCounter();

        Thread thread1 = new Thread(() ->
        {
            IntStream.range(0, 10).forEach(step ->
            {
                int counter = methodSynchrnoziedSafeCounter.increment();
                LOGGER.debug("{} thread1 in {} step with {} count result", Thread.currentThread().getName(), step, counter);

                sleepInMilliSeconds(1000, s -> LOGGER.error(s, Thread.currentThread().getName()), "{} thread is interrupted");
            });
        });

        Thread thread2 = new Thread(() ->
        {
            IntStream.range(0, 20).forEach(step ->
            {
                int counter = methodSynchrnoziedSafeCounter.increment();
                LOGGER.debug("{} thread2 in {} step with {} count result", Thread.currentThread().getName(), step, counter);

                sleepInMilliSeconds(600, s -> LOGGER.error(s, Thread.currentThread().getName()), "{} thread is interrupted");
            });
        });

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        LOGGER.debug("The result should have not ordered integer [1..30] range");
        assertEquals(IntStream.range(1, 31).boxed().collect(Collectors.toList()), methodSynchrnoziedSafeCounter.getResult());
    }

    @Test
    void testSynchrnoziedBlock() throws Exception
    {
        SynchrnoziedBlockSafeCounter synchrnoziedBlockSafeCounter = new SynchrnoziedBlockSafeCounter();

        Thread thread1 = new Thread(() ->
        {
            IntStream.range(0, 10).forEach(step ->
            {
                int counter = synchrnoziedBlockSafeCounter.increment();
                LOGGER.debug("{} thread1 in {} step with {} count result", Thread.currentThread().getName(), step, counter);

                sleepInMilliSeconds(1000, s -> LOGGER.error(s, Thread.currentThread().getName()), "{} thread is interrupted");
            });
        });

        Thread thread2 = new Thread(() ->
        {
            IntStream.range(0, 20).forEach(step ->
            {
                int counter = synchrnoziedBlockSafeCounter.increment();
                LOGGER.debug("{} thread2 in {} step with {} count result", Thread.currentThread().getName(), step, counter);

                sleepInMilliSeconds(600, s -> LOGGER.error(s, Thread.currentThread().getName()), "{} thread is interrupted");
            });
        });

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        LOGGER.debug("The result should have not ordered integer [1..30] range");
        assertEquals(IntStream.range(1, 31).boxed().collect(Collectors.toList()), synchrnoziedBlockSafeCounter.getResult());
    }

    @Test
    void testSynchrnoziedObjectLock() throws Exception
    {
        SynchrnoziedObjectLockSafeCounter synchrnoziedObjectLockSafeCounter = new SynchrnoziedObjectLockSafeCounter();

        Thread thread1 = new Thread(() ->
        {
            IntStream.range(0, 10).forEach(step ->
            {
                int counter = synchrnoziedObjectLockSafeCounter.increment();
                LOGGER.debug("{} thread1 in {} step with {} count result", Thread.currentThread().getName(), step, counter);

                sleepInMilliSeconds(1000, s -> LOGGER.error(s, Thread.currentThread().getName()), "{} thread is interrupted");
            });
        });

        Thread thread2 = new Thread(() ->
        {
            IntStream.range(0, 20).forEach(step ->
            {
                int counter = synchrnoziedObjectLockSafeCounter.increment();
                LOGGER.debug("{} thread2 in {} step with {} count result", Thread.currentThread().getName(), step, counter);

                sleepInMilliSeconds(1000, s -> LOGGER.error(s, Thread.currentThread().getName()), "{} thread is interrupted");
            });
        });

        thread1.start();
        thread2.start();

        thread1.join();
        thread2.join();

        LOGGER.debug("The result should have not ordered integer [1..30] range");
        assertEquals(IntStream.range(1, 31).boxed().collect(Collectors.toList()), synchrnoziedObjectLockSafeCounter.getResult());
    }

    /**
     * The thread safe counter that uses synchronized {@link this#increment()} method to increment counter variable.
     */
    private static class SynchrnoziedMethodSafeCounter
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
     * The thread safe counter that uses synchronized block in {@link this#increment()} method to increment counter variable.
     */
    private static class SynchrnoziedBlockSafeCounter
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
        int increment()
        {
            synchronized (this)
            {
                counter++;
                result.add(counter);
                return counter;
            }
        }
    }

    /**
     * The thread safe counter that uses synchronized object lock in {@link this#increment()} method to increment counter variable.
     */
    private static class SynchrnoziedObjectLockSafeCounter
    {
        private final List<Integer> result = new ArrayList<>();
        private final Object lock = new Object();
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
        int increment()
        {
            synchronized (lock)
            {
                counter++;
                result.add(counter);
                return counter;
            }
        }
    }
}
