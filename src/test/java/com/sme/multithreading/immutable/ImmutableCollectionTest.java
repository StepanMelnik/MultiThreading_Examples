package com.sme.multithreading.immutable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * Unit tests to work with Guava immutable collections.
 */
public class ImmutableCollectionTest
{
    @SuppressWarnings("deprecation")
    @Test
    void testImmutableList() throws Exception
    {
        final ImmutableList<String> immutableSource = ImmutableList.of(
                "red",
                "orange",
                "yellow",
                "green",
                "blue");

        assertThrows(UnsupportedOperationException.class, () -> immutableSource.add("black"));

        final ImmutableList<String> newImmutableSource = ImmutableList.<String> builder()
                .addAll(immutableSource)
                .add("white")
                .build();

        assertEquals(ImmutableList.of(
                "red",
                "orange",
                "yellow",
                "green",
                "blue",
                "white"), newImmutableSource);

        assertThrows(UnsupportedOperationException.class, () -> newImmutableSource.add("black"));
    }

    @SuppressWarnings("deprecation")
    @Test
    void testImmutableSet() throws Exception
    {
        final ImmutableSet<String> immutableSource = ImmutableSet.of(
                "red",
                "orange",
                "yellow",
                "green",
                "blue");

        assertThrows(UnsupportedOperationException.class, () -> immutableSource.add("black"));

        final ImmutableSet<String> newImmutableSource = ImmutableSet.<String> builder()
                .addAll(immutableSource)
                .add("white")
                .build();

        assertThrows(UnsupportedOperationException.class, () -> newImmutableSource.add("black"));
    }

    @SuppressWarnings("deprecation")
    @Test
    void testImmutableMap() throws Exception
    {
        final ImmutableMap<Integer, String> immutableSource = ImmutableMap.of(
                1, "red",
                2, "orange",
                3, "yellow",
                4, "green",
                5, "blue");

        assertThrows(UnsupportedOperationException.class, () -> immutableSource.put(100, "black"));

        final ImmutableMap<Integer, String> newImmutableSource = ImmutableMap.<Integer, String> builder()
                .putAll(immutableSource)
                .put(6, "white")
                .build();

        assertThrows(UnsupportedOperationException.class, () -> newImmutableSource.put(100, "black"));
    }
}
