package com.sme.multithreading.consumerproducer;

import static com.sme.multithreading.util.ThreadUtil.sleepInMilliSeconds;
import static com.sme.multithreading.util.ThreadUtil.sleepInSeconds;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sme.multithreading.countdown.CountDownLatchTest;
import com.sme.multithreading.model.Message;
import com.sme.multithreading.waitnotify.WaitNotifyThreadTest;

/**
 * The test demonstrates how to work with Consumer and Producer using {@link BlockingArray}.
 * <p>
 * This queue orders elements FIFO (first-in-first-out). <br/>
 * {@link ArrayBlockingQueue} works with final predefined capacity. The main Lock based on ReentrantLock implementation with notEmpty and notFull
 * conditions.
 * </p>
 * <p>
 * Compare the solution with low level implementation in {@link WaitNotifyThreadTest}.
 * </p>
 */
public class ConsumerProducerFifoTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CountDownLatchTest.class);

    @Test
    void testConsumerProducerIbBlockingArray() throws InterruptedException
    {
        BlockingQueue<Message> blockingArrays = new ArrayBlockingQueue<>(10);
        CountDownLatch countDownLatch = new CountDownLatch(1);

        Thread producer = new Thread(() ->
        {
            sleepInSeconds(5, s -> LOGGER.error(s, Thread.currentThread().getName()), "{} producer thread is interrupted");

            LOGGER.debug("Starting to send events in producer");
            IntStream.range(0, 100).forEach(step ->
            {
                sleepInMilliSeconds(200, s -> LOGGER.error(s, Thread.currentThread().getName()), "{} consumer thread is interrupted");

                try
                {
                    // Inserts the specified element into this queue, waiting if necessary for space to become available
                    blockingArrays.put(new Message("Message " + step));
                }
                catch (InterruptedException e)
                {
                    LOGGER.error("Put is interrupted while adding {} step in {} thread", step, Thread.currentThread().getName());
                }

                LOGGER.debug("{} remaining capacity in producer", blockingArrays.remainingCapacity());
            });

            // Finishing with delay to allow the consumer fetch all messages.
            sleepInSeconds(3, s -> LOGGER.error(s, Thread.currentThread().getName()), "{} hread is interrupted while finishing");
            countDownLatch.countDown();
        });

        List<Message> consumedMessages = new ArrayList<>();
        Thread consumer = new Thread(() ->
        {
            while (true)
            {
                sleepInMilliSeconds(100, s -> LOGGER.error(s, Thread.currentThread().getName()), "{} consumer thread is interrupted");
                try
                {
                    Message message = blockingArrays.take();
                    LOGGER.debug("Take {} message", message);
                    consumedMessages.add(message);
                }
                catch (InterruptedException e)
                {
                    LOGGER.error("Take is interrupted while ftching element in {} consumer", Thread.currentThread().getName());
                }
            }
        });

        producer.start();
        consumer.start();

        countDownLatch.await();

        assertEquals(100, consumedMessages.size());
        assertEquals(IntStream.range(0, 100).boxed().map(step -> new Message("Message " + step)).collect(Collectors.toList()), consumedMessages);
    }
}
