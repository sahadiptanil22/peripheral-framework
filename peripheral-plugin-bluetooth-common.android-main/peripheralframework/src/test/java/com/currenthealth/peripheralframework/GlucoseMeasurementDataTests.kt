/*
 * GlucoseMeasurementDataTests.kt
 * Android Peripheral Framework
 *
 * Copyright (C) 2022 Current Health. All rights reserved.
 */

package com.currenthealth.peripheralframework

import com.currenthealth.peripheralframework.internal.characteristicdata.GlucoseMeasurementData
import org.junit.Assert
import org.junit.Test
import java.time.LocalDateTime

class GlucoseMeasurementDataTests {
    @Test
    fun testMinimumFromBytes() {
        val bytes = ubyteArrayOf(0b00000000u) // flags
            .plus(ubyteArrayOf(0x01u, 0x00u)) // sequence number, little endian
            .plus(ubyteArrayOf(0xD0u, 0x07u, 0x01u, 0x02u, 0x03u, 0x04u, 0x05u)) // base time

        val expected = GlucoseMeasurementData(
            bytes,
            sequenceNumber = 0x0001u,
            baseTime = LocalDateTime.of(2000, 1, 2, 3, 4, 5)
        )

        Assert.assertEquals(expected, GlucoseMeasurementData.fromBytes(bytes))
    }

    @Test
    fun testKiloGramFromBytes() {
        val bytes = ubyteArrayOf(0b00000010u) // flags
            .plus(ubyteArrayOf(0x01u, 0x00u)) // sequence number, little endian
            .plus(ubyteArrayOf(0xD0u, 0x07u, 0x01u, 0x02u, 0x03u, 0x04u, 0x05u)) // base time
            .plus(ubyteArrayOf(0x34u, (0b00010000u + 0x01u).toUByte())) // 308e1
            .plus(0x1Au) // Type/Location: Control Solution, Finger

        val expected = GlucoseMeasurementData(
            bytes,
            0x0001u,
            LocalDateTime.of(2000, 1, 2, 3, 4, 5),
            glucoseConcentration = 0.0308,
            measurementUnit = GlucoseMeasurementData.GlucoseMeasurementUnit.KG_PER_L,
            measurementType = GlucoseMeasurementData.GlucoseMeasurementType.CONTROL_SOLUTION,
            measurementSampleLocation = GlucoseMeasurementData.GlucoseMeasurementSampleLocation.FINGER
        )

        Assert.assertEquals(expected, GlucoseMeasurementData.fromBytes(bytes))
    }

    @Test
    fun testMolFromBytes() {
        val bytes = ubyteArrayOf(0b00000110u) // flags
            .plus(ubyteArrayOf(0x04u, 0x00u)) // sequence number, little endian
            .plus(ubyteArrayOf(0xD0u, 0x07u, 0x01u, 0x02u, 0x03u, 0x04u, 0x05u)) // base time
            .plus(ubyteArrayOf(0x34u, (0b00010000u + 0x01u).toUByte())) // 308e1
            .plus(0x34u) // Type/Location: EarLobe, Venous Plasma

        val expected = GlucoseMeasurementData(
            bytes,
            0x0004u,
            LocalDateTime.of(2000, 1, 2, 3, 4, 5),
            glucoseConcentration = 3.08,
            measurementUnit = GlucoseMeasurementData.GlucoseMeasurementUnit.MOL_PER_L,
            measurementType = GlucoseMeasurementData.GlucoseMeasurementType.VENOUS_PLASMA,
            measurementSampleLocation = GlucoseMeasurementData.GlucoseMeasurementSampleLocation.EARLOBE
        )

        Assert.assertEquals(expected, GlucoseMeasurementData.fromBytes(bytes))
    }

    @Test
    fun testTimeOffsetFromBytes() {
        val bytes = ubyteArrayOf(0b00000001u) // flags
            .plus(ubyteArrayOf(0x04u, 0x00u)) // sequence number, little endian
            .plus(ubyteArrayOf(0xD0u, 0x07u, 0x01u, 0x02u, 0x03u, 0x04u, 0x05u)) // base time
            .plus(ubyteArrayOf(0x34u, 0x56u))

        val expected = GlucoseMeasurementData(
            bytes,
            0x0004u,
            LocalDateTime.of(2000, 1, 2, 3, 4, 5),
            timeOffset = 0x5634u
        )

        Assert.assertEquals(expected, GlucoseMeasurementData.fromBytes(bytes))
    }

    @Test
    fun testSensorStatusAnnunciationFromBytes() {
        val bytes = ubyteArrayOf(0b00001000u) // flags
            .plus(ubyteArrayOf(0x04u, 0x00u)) // sequence number, little endian
            .plus(ubyteArrayOf(0xD0u, 0x07u, 0x01u, 0x02u, 0x03u, 0x04u, 0x05u)) // base time
            .plus(ubyteArrayOf(0b01010111u, 0b00000101u))

        val expected = GlucoseMeasurementData(
            bytes,
            0x0004u,
            LocalDateTime.of(2000, 1, 2, 3, 4, 5),
            sensorStatusAnnunciation = GlucoseMeasurementData.SensorStatusAnnunciation(
                deviceBatteryLow = true,
                sensorMalfunction = true,
                sampleSizeInsufficient = true,
                stripInsertionError = false,
                stripTypeIncorrect = true,
                sensorResultTooHigh = false,
                sensorResultTooLow = true,
                sensorTemperatureTooHigh = false,
                sensorTemperatureTooLow = true,
                sensorReadInterrupted = false,
                generalDeviceFault = true,
                timeFault = false
            )
        )

        Assert.assertEquals(expected, GlucoseMeasurementData.fromBytes(bytes))
    }

    @Test
    fun testAllFromBytes() {
        val bytes = ubyteArrayOf(0b00001011u) // flags
            .plus(ubyteArrayOf(0x04u, 0x00u)) // sequence number, little endian
            .plus(ubyteArrayOf(0xD0u, 0x07u, 0x01u, 0x02u, 0x03u, 0x04u, 0x05u)) // base time
            .plus(ubyteArrayOf(0x34u, 0x56u))
            .plus(ubyteArrayOf(0x34u, (0b00010000u + 0x01u).toUByte())) // 308e1
            .plus(0x49u) // Type/Location: INTERSTITIAL_FLUID/CONTROL_SOLUTION
            .plus(ubyteArrayOf(0b01010000u, 0b00001111u))

        val expected = GlucoseMeasurementData(
            bytes,
            0x0004u,
            LocalDateTime.of(2000, 1, 2, 3, 4, 5),
            timeOffset = 0x5634u,
            glucoseConcentration = 0.0308,
            measurementUnit = GlucoseMeasurementData.GlucoseMeasurementUnit.KG_PER_L,
            measurementType = GlucoseMeasurementData.GlucoseMeasurementType.INTERSTITIAL_FLUID,
            measurementSampleLocation = GlucoseMeasurementData.GlucoseMeasurementSampleLocation.CONTROL_SOLUTION,
            sensorStatusAnnunciation = GlucoseMeasurementData.SensorStatusAnnunciation(
                deviceBatteryLow = false,
                sensorMalfunction = false,
                sampleSizeInsufficient = false,
                stripInsertionError = false,
                stripTypeIncorrect = true,
                sensorResultTooHigh = false,
                sensorResultTooLow = true,
                sensorTemperatureTooHigh = false,
                sensorTemperatureTooLow = true,
                sensorReadInterrupted = true,
                generalDeviceFault = true,
                timeFault = true
            )
        )

        Assert.assertEquals(expected, GlucoseMeasurementData.fromBytes(bytes))
    }
}
