package com.sme.multithreading.reentrantlock;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The unit tests demonstrate how to safely perform put and get methods in non-thread-safe {@link HashMap} using {@link ReentrantReadWriteLock}.
 */
public class ReentrantReadWriteLockTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ReentrantReadWriteLockTest.class);
    private static final String UNIQUE_KEY = "KEY";

    /**
     * The test creates threads and puts/gets value in HashMap per thread.
     * <p>
     * Thread expects that value is not updated per key.
     * </p>
     */
    @Test
    void testNotThreadSafeHashMap() throws Exception
    {
        final int threadsCount = 5;

        Map<String, Integer> hashMap = new HashMap<>();
        AtomicReference<Throwable> errorReference = new AtomicReference<>();
        List<Thread> threads = new ArrayList<>(threadsCount);

        Thread.UncaughtExceptionHandler uncaughtExceptionHandler = (th, ex) ->
        {
            LOGGER.error("Catched error", ex);

            errorReference.set(ex);
            th.interrupt();
        };

        for (int i = 0; i < threadsCount; i++)
        {
            Thread thread = new Thread(() ->
            {
                IntStream.range(0, 100).forEach(step ->
                {
                    //LOGGER.debug("Put {} key in {} thread", UNIQUE_KEY, Thread.currentThread().getName());
                    hashMap.put(UNIQUE_KEY, step);

                    Integer value = hashMap.get(UNIQUE_KEY);
                    if (value == null)
                    {
                        throw new IllegalAccessError("HashMap has null-object by created key");
                    }

                    if (!value.equals(step))
                    {
                        throw new IllegalAccessError("HashMap has unexprected value by key = " + step);
                    }
                });
            });

            thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
            threads.add(thread);
        }

        threads.forEach(t -> t.start());
        threads.forEach(t ->
        {
            try
            {
                t.join();
            }
            catch (InterruptedException e)
            {
                LOGGER.error("{} thread is interrupted", t.getName(), e);
            }
        });

        TimeUnit.SECONDS.sleep(10);

        assertNotNull(errorReference.get(), "HashMap is in illegal state");
    }

    /**
     * The test creates threads and puts/gets value in HashMapDecorator per thread.
     * <p>
     * Thread expects that HashMapDecorator is thread safe.
     * </p>
     */
    @Test
    void testThreadSafeHashMap() throws Exception
    {
        final int threadsCount = 5;

        HashMapDecorator<String, Integer> hashMapDecorator = new HashMapDecorator<>();
        AtomicReference<Throwable> errorReference = new AtomicReference<>();
        List<Thread> threads = new ArrayList<>(threadsCount);

        Thread.UncaughtExceptionHandler uncaughtExceptionHandler = (th, ex) ->
        {
            LOGGER.error("Catched error", ex);

            errorReference.set(ex);
            th.interrupt();
        };

        for (int i = 0; i < threadsCount; i++)
        {
            Thread thread = new Thread(() ->
            {
                IntStream.range(0, 100).forEach(step ->
                {
                    //LOGGER.debug("Put {} key in {} thread", UNIQUE_KEY, Thread.currentThread().getName());
                    Integer value = hashMapDecorator.putAndGet(UNIQUE_KEY, step);

                    if (value == null)
                    {
                        throw new IllegalAccessError("HashMap has null-object by created key");
                    }

                    if (!value.equals(step))
                    {
                        throw new IllegalAccessError("HashMap has unexprected value by key = " + step);
                    }
                });
            });

            thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
            threads.add(thread);
        }

        threads.forEach(t -> t.start());
        threads.forEach(t ->
        {
            try
            {
                t.join();
            }
            catch (InterruptedException e)
            {
                LOGGER.error("{} thread is interrupted", t.getName(), e);
            }
        });

        TimeUnit.SECONDS.sleep(10);

        assertNull(errorReference.get(), "Expects HashMapDecorator is ThreadSafe");
    }

    /**
     * Provides decorator of {@link HashMap} in thread safe mode.
     * 
     * @param <K> the type of keys maintained by this map
     * @param <V> the type of mapped values
     */
    private static class HashMapDecorator<K, V>
    {
        private final Map<K, V> map = new HashMap<>();

        Lock reentrantLock = new ReentrantLock();
        ReadWriteLock lock = new ReentrantReadWriteLock();
        Lock writeLock = lock.writeLock();
        Lock readLock = lock.readLock();

        /**
         * Put value sing write lock.
         * 
         * @param key The key of map;
         * @param value The value of map.
         */
        @SuppressWarnings("unused")
        void put(K key, V value)
        {
            try
            {
                writeLock.lock();
                map.put(key, value);
            }
            finally
            {
                writeLock.unlock();
            }

        }

        /**
         * Get value by key.
         * 
         * @param key The key of map;
         * @return Returns the fetched value;
         */
        @SuppressWarnings("unused")
        V get(K key)
        {
            try
            {
                readLock.lock();
                return map.get(key);
            }
            finally
            {
                readLock.unlock();
            }
        }

        /**
         * Put and get value in the thread safe mode.
         * 
         * @param key The key of map;
         * @param value The value of map;
         * @return Returns the inserted value.
         */
        V putAndGet(K key, V value)
        {
            try
            {
                reentrantLock.lock();
                map.put(key, value);    // returns value, but the idea of test to fetch value by key
                return map.get(key);
            }
            finally
            {
                reentrantLock.unlock();
            }
        }

    }
}
