/*
 * SFloatTests.kt
 * Android Peripheral Framework
 *
 * Copyright (C) 2022 Current Health. All rights reserved.
 */

package com.currenthealth.peripheralframework

import com.currenthealth.peripheralframework.internal.SFloat
import org.junit.Assert
import org.junit.Test

/**
 * Tests based on information in PERSONAL HEALTH DEVICES TRANSCODING WHITE PAPER V15r00
 * bluetooth.org/DocMan/handlers/DownloadDoc.ashx?doc_id=272346
 */
class SFloatTests {
    @Test
    fun testNaN() {
        val nanShort: Short = 0x07FF
        Assert.assertEquals(Double.NaN, SFloat(nanShort).toDouble(), 0.toDouble())
    }

    @Test
    fun testNRes() {
        val nResShort: Short = 0x0800
        Assert.assertEquals(Double.NaN, SFloat(nResShort).toDouble(), 0.toDouble())
    }

    @Test
    fun testReserved() {
        val nInfinityShort: Short = 0x0801
        Assert.assertEquals(Double.NaN, SFloat(nInfinityShort).toDouble(), 0.toDouble())
    }

    @Test
    fun testInfinity() {
        val infinityShort: Short = 0x07FE
        Assert.assertEquals(Double.POSITIVE_INFINITY, SFloat(infinityShort).toDouble(), 0.toDouble())
    }

    @Test
    fun testNInfinity() {
        val nInfinityShort: Short = 0x0802
        Assert.assertEquals(Double.NEGATIVE_INFINITY, SFloat(nInfinityShort).toDouble(), 0.toDouble())
    }

    @Test
    fun testSFloatDecodingOfficialExample() {
        val encodedShort: Short = 0x0072
        val expectedDecoding: Double = 114.toDouble()

        Assert.assertEquals(expectedDecoding, SFloat(encodedShort).toDouble(), 0.toDouble())
    }

    @Test
    fun testSFloatDecodingPositiveOffset() {
        val encodedShort: Short = 0x0072
        val expectedDecoding: Double = 1140.toDouble()

        Assert.assertEquals(expectedDecoding, SFloat(encodedShort, 1).toDouble(), 0.toDouble())
    }

    @Test
    fun testSFloatDecodingNegativeOffset() {
        val encodedShort: Short = 0x0072
        val expectedDecoding: Double = 1.14

        Assert.assertEquals(expectedDecoding, SFloat(encodedShort, -2).toDouble(), 0.00000000000001.toDouble())
    }
}
