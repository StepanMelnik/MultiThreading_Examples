package com.sme.multithreading.guava;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSortedMap;
import com.google.common.util.concurrent.Striped;

/**
 * Unit tests of locking based on {@link Striped} implementation in Guava.
 */
public class StrippedLockTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(StrippedLockTest.class);

    private static final int SLOTS = 5;
    private static final int THREADS = 1_000;

    private final Lock lock = new ReentrantLock();
    private final Striped<Lock> stripedLock = Striped.lock(2);

    private final StopWatch stopWatch = new StopWatch();

    @BeforeEach
    void setUp()
    {
        stopWatch.reset();
    }

    @Test
    void testSimpleLock() throws Exception
    {
        stopWatch.start();
        HashMap<Integer, String> source = new HashMap<>();
        CompletableFuture<?>[] requests = new CompletableFuture<?>[THREADS * SLOTS];

        for (int i = 0; i < THREADS; i++)
        {
            requests[SLOTS * i + 0] = CompletableFuture.supplyAsync(putLockSupplier(source, i));
            requests[SLOTS * i + 1] = CompletableFuture.supplyAsync(getLockSupplier(source, i));
            requests[SLOTS * i + 2] = CompletableFuture.supplyAsync(getLockSupplier(source, i));
            requests[SLOTS * i + 3] = CompletableFuture.supplyAsync(getLockSupplier(source, i));
            requests[SLOTS * i + 4] = CompletableFuture.supplyAsync(getLockSupplier(source, i));
        }

        LOGGER.debug("Process {} requests", requests.length);
        CompletableFuture.allOf(requests).join();

        stopWatch.stop();

        LOGGER.debug("Time in milliseconds: " + stopWatch.getTime(TimeUnit.MILLISECONDS));

        ImmutableSortedMap<Integer, String> sortedMap = ImmutableSortedMap.copyOf(source, (o1, o2) -> o1.compareTo(o2));

        Map<Integer, String> expectedResult = new HashMap<>();
        IntStream.range(0, THREADS).forEach(i -> expectedResult.put(i, "value" + i));

        assertEquals(expectedResult, sortedMap);
    }

    @Test
    void testStrippedLock() throws Exception
    {
        stopWatch.start();
        HashMap<Integer, String> source = new HashMap<>();
        CompletableFuture<?>[] requests = new CompletableFuture<?>[THREADS * SLOTS];

        for (int i = 0; i < THREADS; i++)
        {
            requests[SLOTS * i + 0] = CompletableFuture.supplyAsync(putStrippedSupplier(source, i));
            requests[SLOTS * i + 1] = CompletableFuture.supplyAsync(getStrippedSupplier(source, i));
            requests[SLOTS * i + 2] = CompletableFuture.supplyAsync(getStrippedSupplier(source, i));
            requests[SLOTS * i + 3] = CompletableFuture.supplyAsync(getStrippedSupplier(source, i));
            requests[SLOTS * i + 4] = CompletableFuture.supplyAsync(getLockSupplier(source, i));
        }

        LOGGER.debug("Process {} requests", requests.length);
        CompletableFuture.allOf(requests).join();

        stopWatch.stop();

        LOGGER.debug("Time in milliseconds: " + stopWatch.getTime(TimeUnit.MILLISECONDS));

        ImmutableSortedMap<Integer, String> sortedMap = ImmutableSortedMap.copyOf(source, (o1, o2) -> o1.compareTo(o2));

        Map<Integer, String> expectedResult = new HashMap<>();
        IntStream.range(0, THREADS).forEach(i -> expectedResult.put(i, "value" + i));

        assertEquals(expectedResult, sortedMap);
    }

    private Supplier<String> putLockSupplier(Map<Integer, String> map, int key)
    {
        return () ->
        {
            lock.lock();
            try
            {
                LOGGER.debug("putLockSupplier: {} key in {} thread", key, Thread.currentThread().getName());
                return map.put(key, "value" + key);
            }
            finally
            {
                lock.unlock();
            }
        };
    }

    private Supplier<String> putStrippedSupplier(Map<Integer, String> map, int key)
    {
        return () ->
        {
            int bucket = key % stripedLock.size();
            Lock lock = stripedLock.get(bucket);
            lock.lock();
            try
            {
                LOGGER.debug("putStrippedSupplier: {} key in {} thread", key, Thread.currentThread().getName());
                return map.put(key, "value" + key);
            }
            finally
            {
                lock.unlock();
            }
        };
    }

    private Supplier<String> getLockSupplier(Map<Integer, String> map, int key)
    {
        return () ->
        {
            lock.lock();
            try
            {
                LOGGER.debug("getLockSupplier: {} key in {} thread", key, Thread.currentThread().getName());
                return map.get(key);
            }
            finally
            {
                lock.unlock();
            }
        };
    }

    private Supplier<String> getStrippedSupplier(Map<Integer, String> map, int key)
    {
        return () ->
        {
            int bucket = key % stripedLock.size();
            Lock lock = stripedLock.get(bucket);
            lock.lock();
            try
            {
                LOGGER.debug("getStrippedSupplier: {} key in {} thread", key, Thread.currentThread().getName());
                return map.get(key);
            }
            finally
            {
                lock.unlock();
            }
        };
    }
}
