package com.sme.multithreading.exceptionhandler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The test demonstrates how to catch exceptions in thread.
 */
public class ThreadUncaughtExceptionHandlerTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadUncaughtExceptionHandlerTest.class);

    @Test
    void testUncaughtExceptionHandler() throws Exception
    {
        AtomicReference<Throwable> errorReference = new AtomicReference<>();

        Thread.UncaughtExceptionHandler uncaughtExceptionHandler = (th, ex) ->
        {
            LOGGER.error("Catched error", ex);

            errorReference.set(ex);
            th.interrupt();
        };

        Thread thread = new Thread(() ->
        {
            LOGGER.debug("Start thread");

            LOGGER.debug("Throw error");
            throw new RuntimeException("The error occurs in inner thread");
        });

        thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
        thread.start();
        thread.join();

        assertEquals("The error occurs in inner thread", errorReference.get().getMessage());
    }
}
