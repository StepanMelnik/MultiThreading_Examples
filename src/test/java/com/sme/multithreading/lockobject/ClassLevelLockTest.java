package com.sme.multithreading.lockobject;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The test demonstrates how to use Class level lock.
 */
public class ClassLevelLockTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectLevelLockTest.class);

    @Test
    void test() throws Exception
    {
        Thread thread1 = new Thread(() ->
        {
            IntStream.range(0, 10).forEach(step ->
            {
                try
                {
                    int counter = SynchrnoziedStaticMethodSafeCounter.increment();

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
                    int counter = SynchrnoziedStaticMethodSafeCounter.increment();

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
        assertEquals(IntStream.range(1, 31).boxed().collect(Collectors.toList()), SynchrnoziedStaticMethodSafeCounter.getResult());
    }

    /**
     * The thread safe counter that uses synchronized {@link this#increment()} method to increment counter variable.
     */
    private static class SynchrnoziedStaticMethodSafeCounter
    {
        private final static List<Integer> result = new ArrayList<>();
        private static int counter;

        /**
         * Get list of created counters.
         * 
         * @return Returns counter list.
         */
        static List<Integer> getResult()
        {
            return result;
        }

        /**
         * Increment variable.
         */
        synchronized static int increment()
        {
            counter++;
            result.add(counter);
            return counter;
        }
    }
}