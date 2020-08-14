package com.sme.multithreading.threadpool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sme.multithreading.model.DelayedMessage;
import com.sme.multithreading.service.SlowService;

/**
 * Unit tests of {@link Executors#newCachedThreadPool}: creates a thread pool that creates new threads as needed, but will reuse previously
 * constructed threads when they are available.
 */
public class CachedThreadPoolTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CachedThreadPoolTest.class);

    private final SlowService slowService = new SlowService();
    private final StopWatch stopWatch = new StopWatch();

    @BeforeEach
    void setUp()
    {
        stopWatch.reset();
    }

    /**
     * <pre>
     * Test plan: 
     * 1) create cached thread pool with undefined count of threads in the pool; 
     * 2) create a list of tasks 
     * 3) perform all tasks in the cached thread pool
     * 4) Assert how many threads created, the most slowest threads, etc.
     * </pre>
     */
    @Test
    void testCachedThreadPool() throws Exception
    {
        final int initialCapacity = 1000;
        List<Callable<DelayedMessage>> tasks = new ArrayList<>(initialCapacity);

        IntStream.range(1, initialCapacity).forEach(step ->
        {
            tasks.add(() -> slowService.getMessage(step));
        });

        // no count of threads with own thread factory (Guava ThreadFactoryBuilder does not have a solution to count of used threads)
        CountThreadFactory countThreadFactory = new CountThreadFactory();
        ExecutorService executorService = Executors.newCachedThreadPool(countThreadFactory);

        stopWatch.start();
        List<Future<DelayedMessage>> futureResult = executorService.invokeAll(tasks, 1_500, TimeUnit.MILLISECONDS);
        executorService.shutdown();

        List<DelayedMessage> result = futureResult.stream()
                .map(t ->
                {
                    try
                    {
                        return t.get();
                    }
                    catch (InterruptedException | ExecutionException e)
                    {
                        LOGGER.error("Cannot fetch a value", e);
                        throw new RuntimeException(e);
                    }
                })
                .sorted(Comparator.comparing(DelayedMessage::getDelay))
                .collect(Collectors.toList());

        stopWatch.stop();

        LOGGER.debug("Cached thread pool created {} threads", countThreadFactory.getCount());
        assertTrue(initialCapacity > countThreadFactory.getCount(), "Expects no more created threads that predefined capacity");

        LOGGER.debug("Time in seconds: " + stopWatch.getTime(TimeUnit.MILLISECONDS));
        assertEquals(999, result.size());

        LOGGER.debug("Get the slowest {} message", result.stream().sorted(Comparator.comparing(DelayedMessage::getDelay).reversed()).limit(5).collect(Collectors.toList()));

        assertTrue(stopWatch.getTime(TimeUnit.MILLISECONDS) > result.stream().findFirst().get().getDelay());
    }

    /**
     * Thread factory to count created threads.
     */
    private static class CountThreadFactory implements ThreadFactory
    {
        private final AtomicInteger counter = new AtomicInteger();

        @Override
        public Thread newThread(Runnable r)
        {
            String name = "CountThreadFactory " + counter.getAndIncrement();
            LOGGER.debug("Create new thread with {} name", name);
            return new Thread(r, name);
        }

        /**
         * Returns a count of created threads.
         */
        int getCount()
        {
            return counter.get();
        }
    }
}
