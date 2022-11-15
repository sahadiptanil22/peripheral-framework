/*
 * UByteQueueTests.kt
 * Android Peripheral Framework
 *
 * Copyright (C) 2022 Current Health. All rights reserved.
 */

package com.currenthealth.peripheralframework

import com.currenthealth.peripheralframework.internal.UByteQueue
import org.junit.Assert
import org.junit.Test

class UByteQueueTests {
    @Test
    fun testSize() {
        val testBytes = UByteArray(7) { 4u }

        Assert.assertEquals(testBytes.size, UByteQueue(testBytes).size)
    }

    @Test
    fun testPop() {
        val testBytes = ubyteArrayOf(0u, 1u, 2u, 3u, 4u, 5u)
        val popSize = 3
        val expected = testBytes.sliceArray(0 until popSize)

        val queue = UByteQueue(testBytes)

        Assert.assertArrayEquals(expected.toByteArray(), queue.pop(popSize)?.toByteArray())
    }

    @Test
    fun testPopAll() {
        val testBytes = ubyteArrayOf(0u, 1u, 2u, 3u, 4u, 5u)

        val queue = UByteQueue(testBytes)

        Assert.assertArrayEquals(testBytes.toByteArray(), queue.pop(testBytes.size)?.toByteArray())
    }

    @Test
    fun testPopExceeded() {
        val testBytes = ubyteArrayOf(0u, 1u, 2u, 3u, 4u, 5u)

        val queue = UByteQueue(testBytes)

        Assert.assertNull(queue.pop(testBytes.size + 1))
    }

    @Test
    fun testPopZero() {
        val testBytes = ubyteArrayOf(0u, 1u, 2u, 3u, 4u, 5u)

        val queue = UByteQueue(testBytes)

        Assert.assertArrayEquals(UByteArray(0).toByteArray(), queue.pop(0)?.toByteArray())
    }

    @Test
    fun testPopTwice() {
        val testBytes = ubyteArrayOf(0u, 1u, 2u, 3u, 4u, 5u)
        val popSize = 3
        val expected1 = testBytes.sliceArray(0 until popSize)
        val expected2 = testBytes.sliceArray(popSize until testBytes.size)

        val queue = UByteQueue(testBytes)

        Assert.assertArrayEquals(expected1.toByteArray(), queue.pop(popSize)?.toByteArray())
        Assert.assertArrayEquals(expected2.toByteArray(), queue.pop(testBytes.size - popSize)?.toByteArray())
    }

    @Test
    fun testEmptyQueue() {
        val queue = UByteQueue(UByteArray(0))

        Assert.assertEquals(0, queue.size)
        Assert.assertNull(queue.pop(1))
    }
}
