package com.sme.multithreading.executorservice;

import static com.sme.multithreading.util.ThreadUtil.sleepInMilliSeconds;
import static com.sme.multithreading.util.ThreadUtil.sleepInSeconds;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit test demonstrates {@link ExecutorService} features.
 */
public class ExecutorServiceTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorServiceTest.class);

    @Test
    void testPerformTask() throws Exception
    {
        Runnable runnableTask = () ->
        {
            sleepInSeconds(1, m -> LOGGER.error(m, Thread.currentThread().getName()), "{} thread is interrupted");
            LOGGER.debug("Perfrom Runnable logic in {} thread", Thread.currentThread().getName());
        };

        Callable<String> callableTask = () ->
        {
            sleepInMilliSeconds(1500, m -> LOGGER.error(m, Thread.currentThread().getName()), "{} thread is interrupted");
            LOGGER.debug("Perfrom Callable logic in {} thread", Thread.currentThread().getName());
            return "Callable message";
        };

        ExecutorService executor = Executors.newFixedThreadPool(3);
        executor.execute(runnableTask);

        Future<String> result = executor.submit(callableTask);
        assertEquals("Callable message", result.get());

        List<Future<String>> list = executor.invokeAll(asList(callableTask, callableTask, callableTask));
        String listResult = list.stream().map(f ->
        {
            try
            {
                return f.get();
            }
            catch (InterruptedException | ExecutionException e)
            {
                LOGGER.error("Cannot get a result", e);
                throw new RuntimeException(e);
            }
        }).collect(Collectors.joining(", ", "[", "]"));

        executor.shutdown();
        if (!executor.awaitTermination(10, TimeUnit.SECONDS))
        {
            executor.shutdownNow();
        }

        assertEquals("[Callable message, Callable message, Callable message]", listResult);
    }
}
