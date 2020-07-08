package com.sme.multithreading.completablefuture;

import static com.sme.multithreading.util.ThreadUtil.sleepInSeconds;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit tests to work with {@link CompletableFuture} features.
 */
public class CompletableFutureTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CompletableFutureTest.class);

    @Test
    void testComplete() throws Exception
    {
        CompletableFuture<String> completableFuture = new CompletableFuture<>();

        Future<String> future = Executors.newCachedThreadPool().submit(() ->
        {
            TimeUnit.SECONDS.sleep(5);
            completableFuture.complete("Hello CompletableFuture");
            return null;
        });

        assertFalse(completableFuture.isDone(), "Expects false, because future is not completed");
        assertNull(future.get(), "Expects null value");
        assertTrue(completableFuture.isDone(), "Expects completed future");
        assertEquals("Hello CompletableFuture", completableFuture.get());
    }

    @Test
    void testCancel() throws Exception
    {
        CompletableFuture<String> completableFuture = new CompletableFuture<>();

        Executors.newCachedThreadPool().submit(() ->
        {
            TimeUnit.SECONDS.sleep(1);
            completableFuture.cancel(false);
            return null;
        });

        assertThrows(RuntimeException.class, () -> completableFuture.get());
    }

    @Test
    void testWithEncapsulatedComputationLogic() throws Exception
    {
        // Returns a new CompletableFuture that is asynchronously performs a task running in the ForkJoinPool.commonPool()
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> "Hello");
        assertEquals("Hello", future.get());

        CompletableFuture<String> helloWorldFuture = CompletableFuture.supplyAsync(() -> "Hello")
                .thenApplyAsync(s -> s.toUpperCase() + " WORLD");

        helloWorldFuture
                .thenAcceptAsync(s -> LOGGER.debug("{} result in async {} thread", s, Thread.currentThread().getName()))
                .thenRunAsync(() -> LOGGER.debug("Complete in async {} thread", Thread.currentThread().getName()));

        assertEquals("HELLO WORLD", helloWorldFuture.get());
    }

    @Test
    void testJoin() throws Exception
    {
        String result = Stream.of(CompletableFuture.supplyAsync(() -> "One"), CompletableFuture.supplyAsync(() -> "Two"), CompletableFuture.supplyAsync(() -> "Three"))
                //.map(CompletableFuture::join)
                .map(cf -> cf.join())
                .map(String::toUpperCase)
                .collect(Collectors.joining(", "));

        assertEquals("ONE, TWO, THREE", result);
    }

    @Test
    void testApplyAsyncWithExecutor() throws Exception
    {
        ExecutorService executor = Executors.newFixedThreadPool(2, new ThreadFactory()
        {
            private final AtomicInteger atomicInteger = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable runnable)
            {
                return new Thread(runnable, "OwnExecutor-" + atomicInteger.getAndIncrement());
            }
        });

        CompletableFuture<String> completableFuture = CompletableFuture
                .completedFuture("Hello")
                .thenApplyAsync(s ->
                {
                    LOGGER.debug("Process {} value in {} thread", s, Thread.currentThread().getName());
                    assertTrue(Thread.currentThread().getName().startsWith("OwnExecutor-"), "Expects to run logic in own executor");

                    sleepInSeconds(5, m -> LOGGER.error(m, Thread.currentThread().getName()), "{} thread is interrupted");
                    return s.toUpperCase();
                }, executor)
                .thenApplyAsync(s -> s + " WORLD");

        assertNull(completableFuture.getNow(null), "Expects null, because the future is perfromed with delay");
        assertEquals("HELLO WORLD", completableFuture.join());
    }

    @Test
    void testHandleExceptionOnCancel() throws Exception
    {
        CompletableFuture<String> completableFuture = CompletableFuture.completedFuture("Hello")
                .thenApplyAsync(s ->
                {
                    sleepInSeconds(3, m -> LOGGER.error(m, Thread.currentThread().getName()), "{} thread is interrupted");
                    return s.toUpperCase();
                });

        CompletableFuture<String> exceptionHandler = completableFuture.handle((s, th) ->
        {
            LOGGER.error("{} value processed with CancellationException", s, th);
            return (th != null) ? "The result canceled" : "";
        });

        assertTrue(completableFuture.cancel(true), "Expects canceled request");
        assertEquals("The result canceled", exceptionHandler.join());
    }
}
