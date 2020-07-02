package com.sme.multithreading.waitnotify;

import static com.sme.multithreading.util.ThreadUtil.sleepInSeconds;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sme.multithreading.model.Message;

/**
 * Unit test to demonstrate how wait/notify/notifyAll methods communicate between threads.
 */
public class WaitNotifyThreadTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(WaitNotifyThreadTest.class);

    @Test
    void testWaitNotify() throws Exception
    {
        AtomicReference<String> messageInConsumer1 = new AtomicReference<>();
        AtomicReference<String> messageInConsumer2 = new AtomicReference<>();
        Message message = new Message();

        final String messageValue = "This is a message";

        Thread producer = new Thread(() ->
        {
            LOGGER.debug("Start notifier in {} thread", Thread.currentThread().getName());
            sleepInSeconds(5, s -> LOGGER.error(s, Thread.currentThread().getName()), "{} thread is interrupted. Make cleanup");

            synchronized (message)  // notify, notifyAll must be synchronized
            {
                message.setMessage(messageValue);
                // message.notify(); // The logic uses two consumers, so we should use notifyAll, otherwise a waiting thread never finishes
                message.notifyAll();
                LOGGER.debug("Sent a notification in producer for waiting threads", Thread.currentThread().getName());
            }

            LOGGER.debug("Finish producer {} thread", Thread.currentThread().getName());
        });

        Thread consumer1 = new Thread(() ->
        {
            LOGGER.debug("Start consumer1 in {} thread", Thread.currentThread().getName());

            synchronized (message)  // wait must be synchronized
            {
                try
                {
                    message.wait();
                    LOGGER.debug("Wake up consumer1 {} thread", Thread.currentThread().getName());  // allows to continue with
                }
                catch (InterruptedException e)
                {
                    LOGGER.error("{} thread is interrupted in wait()", Thread.currentThread().getName());
                }

                messageInConsumer1.set(message.getMessage());
                LOGGER.debug("Get \"{}\" message in consumer1", message.getMessage());
            }
        });

        Thread consumer2 = new Thread(() ->
        {
            LOGGER.debug("Start consumer2 in {} thread", Thread.currentThread().getName());

            synchronized (message)  // wait must be synchronized
            {
                try
                {
                    message.wait();
                    LOGGER.debug("Wake up consumer2 {} thread", Thread.currentThread().getName());  // allows to continue with
                }
                catch (InterruptedException e)
                {
                    LOGGER.error("{} thread is interrupted in wait()", Thread.currentThread().getName());
                }

                messageInConsumer2.set(message.getMessage());
                LOGGER.debug("Get \"{}\" message in consumer2", message.getMessage());
                LOGGER.debug("Get \"{}\" message one more in consumer2", message.getMessage());
            }
        });

        producer.start();
        consumer1.start();
        consumer2.start();

        producer.join();
        consumer1.join();
        consumer2.join();

        assertEquals(messageValue, messageInConsumer1.get());
        assertEquals(messageValue, messageInConsumer2.get());
    }
}
