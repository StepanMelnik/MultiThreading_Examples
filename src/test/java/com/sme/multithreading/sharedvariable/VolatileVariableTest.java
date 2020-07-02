package com.sme.multithreading.sharedvariable;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit tests show how to work with volatile (shared variable) in multi threading.
 */
public class VolatileVariableTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(VolatileVariableTest.class);

    /**
     * <p>
     * Volatile member is shared in main memory that consumes extra memory. Also it's not easy to debug a program with Volatile members if any problem
     * occurs.
     * </p>
     * <p>
     * The test creates Server instance with isRunning volatile variable. According to the variable we can start or stop Server thread.
     * </p>
     */
    @Test
    void testSharedVariable()
    {
        Server server = new Server();

        Thread thread = new Thread(server);
        thread.start();

        assertTrue(thread.isAlive(), "Expects alive thread");
        assertFalse(thread.isInterrupted(), "Expects not interrupted thread");

        try
        {
            TimeUnit.SECONDS.sleep(10);
        }
        catch (InterruptedException e)
        {
            LOGGER.debug("Test is interrupted", e);
        }

        server.stop();

        try
        {
            TimeUnit.SECONDS.sleep(3);
        }
        catch (InterruptedException e)
        {
            LOGGER.debug("Test is interrupted", e);
        }

        assertFalse(thread.isAlive(), "Expects alive thread");
        assertFalse(thread.isInterrupted(), "Expects not interrupted thread");
    }

    /**
     * Server instance that supports start() and stop() methods;
     */
    private static class Server implements Runnable
    {
        private volatile boolean isRunning = true;

        @Override
        public void run()
        {
            while (isRunning)
            {
                LOGGER.debug("Run in Server");
                try
                {
                    TimeUnit.SECONDS.sleep(1);
                }
                catch (InterruptedException e)
                {
                    LOGGER.debug("Server is interrupted", e);
                }
            }
        }

        /**
         * Stop server.
         */
        void stop()
        {
            LOGGER.debug("Stop Server");
            isRunning = false;
        }
    }
}
