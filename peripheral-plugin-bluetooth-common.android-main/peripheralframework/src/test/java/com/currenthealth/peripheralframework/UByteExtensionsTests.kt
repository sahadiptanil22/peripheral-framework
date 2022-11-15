/*
 * UByteExtensionsTests.kt
 * Android Peripheral Framework
 *
 * Copyright (C) 2022 Current Health. All rights reserved.
 */

package com.currenthealth.peripheralframework

import com.currenthealth.peripheralframework.internal.extensions.bitFlagToBoolean
import org.junit.Assert
import org.junit.Test

class UByteExtensionsTests {
    @Test
    fun testBitFlagToBoolean() {
        val flag: UByte = 0b01010101u

        Assert.assertTrue(flag.bitFlagToBoolean(0))
        Assert.assertFalse(flag.bitFlagToBoolean(1))
        Assert.assertTrue(flag.bitFlagToBoolean(2))
        Assert.assertFalse(flag.bitFlagToBoolean(3))
        Assert.assertTrue(flag.bitFlagToBoolean(4))
        Assert.assertFalse(flag.bitFlagToBoolean(5))
        Assert.assertTrue(flag.bitFlagToBoolean(6))
        Assert.assertFalse(flag.bitFlagToBoolean(7))
    }
}
