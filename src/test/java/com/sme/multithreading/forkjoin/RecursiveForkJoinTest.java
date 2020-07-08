package com.sme.multithreading.forkjoin;

import static com.sme.multithreading.util.ThreadUtil.sleepInMilliSeconds;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit tests to show how RecursiveAction and RecursiveTask work in Fork-Join framework.
 */
public class RecursiveForkJoinTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(RecursiveForkJoinTest.class);

    private int[] arr;
    private CustomRecursiveTask customRecursiveTask;

    @BeforeEach
    public void init()
    {
        Random random = new Random();
        arr = new int[10];
        for (int i = 0; i < arr.length; i++)
        {
            arr[i] = random.nextInt(5);
        }
        customRecursiveTask = new CustomRecursiveTask(arr);
    }

    @Test
    void testInvokeRecursiveAction() throws Exception
    {
        //ForkJoinPool commonPool = ForkJoinPool.commonPool();   //based on count of cores
        ForkJoinPool forkJoinPool = new ForkJoinPool(2);

        assertEquals(2, forkJoinPool.getParallelism());

        CustomRecursiveAction customRecursiveAction = new CustomRecursiveAction("xxxxzzzbbbbaaaa");
        forkJoinPool.invoke(customRecursiveAction); // fork + join operatons

        assertTrue(customRecursiveAction.isDone(), "Expects pool finished");
    }

    @Test
    public void testJoin()
    {
        //ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();
        ForkJoinPool forkJoinPool = new ForkJoinPool(2);

        forkJoinPool.execute(customRecursiveTask);
        int result1 = customRecursiveTask.join();

        LOGGER.debug("Result is {} in the first stage", result1);
        assertTrue(customRecursiveTask.isDone());

        forkJoinPool.submit(customRecursiveTask);
        int result2 = customRecursiveTask.join();

        LOGGER.debug("Result is {} in the second stage", result2);
        assertTrue(customRecursiveTask.isDone());

        // customRecursiveTask creates the same array, so we should get the same result in both stages
        assertEquals(result1, result2);
    }

    @Test
    public void testForkJoin()
    {
        CustomRecursiveTask customRecursiveTaskFirst = new CustomRecursiveTask(arr);
        CustomRecursiveTask customRecursiveTaskSecond = new CustomRecursiveTask(arr);
        CustomRecursiveTask customRecursiveTaskThird = new CustomRecursiveTask(arr);

        customRecursiveTaskFirst.fork();
        customRecursiveTaskSecond.fork();
        customRecursiveTaskThird.fork();

        sleepInMilliSeconds(1000, s -> LOGGER.error(s, Thread.currentThread().getName()), "{} thread is interrupted");

        assertTrue(customRecursiveTaskFirst.isDone());
        assertTrue(customRecursiveTaskSecond.isDone());
        assertTrue(customRecursiveTaskThird.isDone());

        int sum = IntStream.of(customRecursiveTaskFirst.join(), customRecursiveTaskSecond.join(), customRecursiveTaskThird.join())
                .peek(i ->
                {
                    LOGGER.debug("Peek {} value", i);
                    ;
                })
                .sum();

        assertTrue(sum > 0);
    }

    /**
     * Represents action that do not yield a return value, like a {@link Runnable}.
     */
    private static class CustomRecursiveAction extends RecursiveAction
    {
        private String workLoad = "";
        private static final int THRESHOLD = 4;

        public CustomRecursiveAction(String workLoad)
        {
            this.workLoad = workLoad;
        }

        @Override
        protected void compute()
        {
            LOGGER.debug("Start to compute {} value in the {} thread", workLoad, Thread.currentThread().getName());
            if (workLoad.length() > THRESHOLD)
            {
                LOGGER.debug("Create subtasks of {} value in the {} thread", workLoad, Thread.currentThread().getName());
                ForkJoinTask.invokeAll(createSubtasks());   // schedule all for asynchronous execution
            }
            else
            {
                LOGGER.debug("Pcessing {} value in the {} thread", workLoad, Thread.currentThread().getName());
                processing(workLoad);
            }
        }

        private Collection<CustomRecursiveAction> createSubtasks()
        {
            List<CustomRecursiveAction> subtasks = new ArrayList<>();

            String partOne = workLoad.substring(0, workLoad.length() / 2);
            String partTwo = workLoad.substring(workLoad.length() / 2, workLoad.length());

            subtasks.add(new CustomRecursiveAction(partOne));
            subtasks.add(new CustomRecursiveAction(partTwo));

            return subtasks;
        }

        private void processing(String work)
        {
            String result = work.toUpperCase();
            LOGGER.debug("This result - (" + result + ") - was processed by " + Thread.currentThread().getName());
        }
    }

    /**
     * Represents task that yield return values, like a {@link Callable}.
     */
    private static class CustomRecursiveTask extends RecursiveTask<Integer>
    {
        private static final int THRESHOLD = 3;

        private final int[] arr;

        public CustomRecursiveTask(int[] arr)
        {
            this.arr = arr;
        }

        @Override
        protected Integer compute()
        {
            LOGGER.debug("Start to compute {} value in the {} thread", arr, Thread.currentThread().getName());

            if (arr.length > THRESHOLD)
            {
                LOGGER.debug("Create subtasks of {} value in the {} thread", arr, Thread.currentThread().getName());

                Collection<CustomRecursiveTask> collection = ForkJoinTask.invokeAll(createSubtasks());  // schedule for asynchronous execution
                int sum = collection.stream().mapToInt(ForkJoinTask::join).sum();   // returns the result of the computation when it is done
                LOGGER.debug("Create {} sum in subtasks in the {} thread", sum, Thread.currentThread().getName());
                return sum;
            }
            else
            {
                LOGGER.debug("Process {} value in the {} thread", arr, Thread.currentThread().getName());
                return processing(arr);
            }
        }

        private Collection<CustomRecursiveTask> createSubtasks()
        {
            List<CustomRecursiveTask> dividedTasks = new ArrayList<>();
            dividedTasks.add(new CustomRecursiveTask(Arrays.copyOfRange(arr, 0, arr.length / 2)));
            dividedTasks.add(new CustomRecursiveTask(Arrays.copyOfRange(arr, arr.length / 2, arr.length)));
            return dividedTasks;
        }

        private Integer processing(int[] arr)
        {
            int sum = Arrays.stream(arr).filter(a -> a > 1 && a < 5).sum();
            LOGGER.debug("Process {} value: filter values in [1..5] range and sum them in the {} thread. Sum is {}", arr, Thread.currentThread().getName(), sum);
            return sum;
        }
    }
}
