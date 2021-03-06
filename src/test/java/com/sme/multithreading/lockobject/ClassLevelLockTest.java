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
                int counter = SynchrnoziedStaticMethodSafeCounter.increment();
                LOGGER.debug("{} thread1 in {} step with {} count result", Thread.currentThread().getName(), step, counter);

                sleepInMilliSeconds(1000, s -> LOGGER.error(s, Thread.currentThread().getName()), "{} thread is interrupted");
            });
        });

        Thread thread2 = new Thread(() ->
        {
            IntStream.range(0, 20).forEach(step ->
            {
                int counter = SynchrnoziedStaticMethodSafeCounter.increment();
                LOGGER.debug("{} thread2 in {} step with {} count result", Thread.currentThread().getName(), step, counter);

                sleepInMilliSeconds(600, s -> LOGGER.error(s, Thread.currentThread().getName()), "{} thread is interrupted");
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
        private static final List<Integer> RESULT = new ArrayList<>();
        private static int COUNTER;

        /**
         * Get list of created counters.
         * 
         * @return Returns counter list.
         */
        static List<Integer> getResult()
        {
            return RESULT;
        }

        /**
         * Increment variable.
         * 
         * @return Returns the variable value.
         */
        static synchronized int increment()
        {
            COUNTER++;
            RESULT.add(COUNTER);
            return COUNTER;
        }
    }
}
