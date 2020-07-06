package com.sme.multithreading.forkjoin;

import static java.util.Arrays.asList;
import static java.util.Collections.EMPTY_LIST;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fork-Join demo to calculate a sum in the tree of nodes.
 */
public class RecursiveNodeForkJoinTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(RecursiveNodeForkJoinTest.class);

    @Test
    void testInvokeRecursiveAction() throws Exception
    {
        //ForkJoinPool commonPool = ForkJoinPool.commonPool();   //based on count of core
        ForkJoinPool forkJoinPool = new ForkJoinPool(2);
        assertEquals(2, forkJoinPool.getParallelism());

        Node node1 = new Node(1, asList(new Node(10, EMPTY_LIST), new Node(11, EMPTY_LIST)));
        Node node2 = new Node(2, asList(new Node(20, EMPTY_LIST), new Node(21, EMPTY_LIST), new Node(22, EMPTY_LIST)));
        Node node3 = new Node(3, asList(new Node(30, EMPTY_LIST)));

        Node root = new Node(0, asList(node1, node2, node3));

        NodeSumCounter task = new NodeSumCounter(root);
        Integer sum = forkJoinPool.invoke(task);

        assertTrue(task.isDone(), "Expects pool finished");
        assertEquals(120, sum);
    }

    /**
     * Represents a tree of nodes.
     */

    private static final class Node
    {
        private final int index;
        private final Collection<Node> nodes;

        Node(int index, Collection<Node> nodes)
        {
            this.nodes = nodes;
            this.index = index;
        }

        Collection<Node> getNodes()
        {
            return nodes;
        }

        int getIndex()
        {
            return index;
        }

        @Override
        public String toString()
        {
            return "Node [index=" + index + ", nodes=" + nodes + "]";
        }
    }

    /**
     * Represents task that yield return values, like a {@link Callable}.
     */
    private static class NodeSumCounter extends RecursiveTask<Integer>
    {
        private final Node node;

        NodeSumCounter(Node node)
        {
            this.node = node;
        }

        @Override
        protected Integer compute()
        {
            LOGGER.debug("Start to compute {} node in the {} thread", node, Thread.currentThread().getName());

            List<NodeSumCounter> subTasks = new LinkedList<>();

            for (Node child : node.getNodes())
            {
                LOGGER.debug("Start to split {} child in the {} thread", child, Thread.currentThread().getName());
                NodeSumCounter task = new NodeSumCounter(child);
                task.fork();    // schedule for asynchronous execution
                subTasks.add(task);
            }

            int childrenSum = subTasks.stream().mapToInt(NodeSumCounter::join).sum();   // returns the result of the computation when it is done
            LOGGER.debug("Children sum = {} and index = {} in {} node in the {} thread", childrenSum, node.getIndex(), node, Thread.currentThread().getName());
            return node.getIndex() + childrenSum;
        }
    }
}
