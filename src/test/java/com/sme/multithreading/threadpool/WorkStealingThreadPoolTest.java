package com.sme.multithreading.threadpool;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sme.multithreading.model.DelayedMessage;
import com.sme.multithreading.service.SlowService;

/**
 * <p>
 * It’s based on a work-stealing algorithm, where a task can spawn other, smaller tasks, which are added to queues of parallel processing threads. If
 * one thread has finished its work and has nothing more to do, it can "steal" the work from the other thread’s queue.
 * </p>
 * Work-stealing mechanism is already used by ForkJoinPool in Java and is highly useful when your task(s) spawn smaller tasks.
 */
public class WorkStealingThreadPoolTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkStealingThreadPoolTest.class);

    private final SlowService slowService = new SlowService();
    private final StopWatch stopWatch = new StopWatch();

    @Test
    void testWorkStealingThreadPool() throws Exception
    {
        // We can create the pool by the following:
        // ExecutorService workStealingPool = Executors.newWorkStealingPool(); // FIFO queue
        ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();    //  LIFO queue
        final int count = 1000;

        stopWatch.start();

        int[] array = IntStream.range(0, count).toArray();
        MessageRecursiveTask recursiveTask = new MessageRecursiveTask(array);

        List<DelayedMessage> result = forkJoinPool.invoke(recursiveTask); // fork and join
        stopWatch.stop();

        LOGGER.debug("Time in seconds: " + stopWatch.getTime(TimeUnit.MILLISECONDS));
        assertEquals(count, result.size());

        LOGGER.debug("Created active thread count: {}", forkJoinPool.getActiveThreadCount());
        LOGGER.debug("Used parallelism: {}", forkJoinPool.getParallelism());
        LOGGER.debug("Used pool size: {}", forkJoinPool.getPoolSize());
        LOGGER.debug("Used steal count: {}", forkJoinPool.getStealCount());
    }

    /**
     * {@link RecursiveTask} implementation to run in Fork-Join pool recursively.
     */
    private class MessageRecursiveTask extends RecursiveTask<List<DelayedMessage>>
    {
        private final int[] ids;

        MessageRecursiveTask(int[] ids)
        {
            this.ids = ids;
        }

        @Override
        protected List<DelayedMessage> compute()
        {
            List<DelayedMessage> list = new ArrayList<>();

            if (ids.length > 1)
            {
                Collection<MessageRecursiveTask> collection = ForkJoinTask.invokeAll(createSubtasks());   // schedule all for asynchronous execution
                list.addAll(collection.stream()
                        .map(ForkJoinTask::join)
                        .flatMap(List::stream)
                        .collect(Collectors.toList()));
            }
            else
            {
                list.add(process(ids[0]));
            }

            return list;
        }

        private DelayedMessage process(int id)
        {
            return slowService.getMessage(id);
        }

        private List<MessageRecursiveTask> createSubtasks()
        {
            List<MessageRecursiveTask> subTasks = new ArrayList<>();

            int length = ids.length;
            int[] left = Arrays.copyOfRange(ids, 0, (length + 1) / 2);
            int[] right = Arrays.copyOfRange(ids, (length + 1) / 2, length);

            subTasks.add(new MessageRecursiveTask(left));
            subTasks.add(new MessageRecursiveTask(right));

            return subTasks;
        }
    }
}
