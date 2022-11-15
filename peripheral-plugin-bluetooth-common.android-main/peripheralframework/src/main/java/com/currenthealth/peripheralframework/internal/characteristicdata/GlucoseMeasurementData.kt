/*
 * GlucoseMeasurementData.kt
 * Android Peripheral Framework
 *
 * Copyright (C) 2022 Current Health. All rights reserved.
 */

package com.currenthealth.peripheralframework.internal.characteristicdata

import com.currenthealth.peripheralframework.internal.SFloat
import com.currenthealth.peripheralframework.internal.UByteQueue
import com.currenthealth.peripheralframework.internal.extensions.Endianness
import com.currenthealth.peripheralframework.internal.extensions.bitFlagToBoolean
import com.currenthealth.peripheralframework.internal.extensions.toUShort
import java.time.LocalDateTime

/**
 * Parses Data from Standard Bluetooth GATT Characteristic, Glucose Measurement
 * based on GATT Specification Supplement V5:
 * https://www.bluetooth.com/specifications/specs/gatt-specification-supplement-5/
 */
data class GlucoseMeasurementData internal constructor(
    val rawBytes: UByteArray,
    val sequenceNumber: UShort,
    val baseTime: LocalDateTime,
    val timeOffset: UShort? = null,
    val glucoseConcentration: Double? = null,
    val measurementUnit: GlucoseMeasurementUnit? = null,
    val measurementType: GlucoseMeasurementType? = null,
    val measurementSampleLocation: GlucoseMeasurementSampleLocation? = null,
    val sensorStatusAnnunciation: SensorStatusAnnunciation? = null
) {
    enum class GlucoseMeasurementUnit {
        KG_PER_L,
        MOL_PER_L
    }

    enum class GlucoseMeasurementType(val byte: UByte) {
        RFU(0x0u),
        CAPILLARY_WHOLE_BLOOD(0x1u),
        CAPILLARY_PLASMA(0x2u),
        VENOUS_WHOLE_BLOOD(0x3u),
        VENOUS_PLASMA(0x4u),
        ARTERIAL_WHOLE_BLOOD(0x5u),
        ARTERIAL_PLASMA(0x6u),
        UNDETERMINED_WHOLE_BLOOD(0x7u),
        UNDETERMINED_PLASMA(0x8u),
        INTERSTITIAL_FLUID(0x9u),
        CONTROL_SOLUTION(0xAu);

        companion object {
            fun fromByte(byte: UByte): GlucoseMeasurementType? {
                return values().firstOrNull { it.byte == byte }
            }
        }
    }

    enum class GlucoseMeasurementSampleLocation(val byte: UByte) {
        RFU(0x0u),
        FINGER(0x1u),
        ALTERNATE_TEST_SITE(0x2u),
        EARLOBE(0x3u),
        CONTROL_SOLUTION(0x4u),
        UNAVAILABLE(0xFu);

        companion object {
            fun fromByte(byte: UByte): GlucoseMeasurementSampleLocation? {
                return values().firstOrNull { it.byte == byte }
            }
        }
    }

    data class SensorStatusAnnunciation(
        val deviceBatteryLow: Boolean,
        val sensorMalfunction: Boolean,
        val sampleSizeInsufficient: Boolean,
        val stripInsertionError: Boolean,
        val stripTypeIncorrect: Boolean,
        val sensorResultTooHigh: Boolean,
        val sensorResultTooLow: Boolean,
        val sensorTemperatureTooHigh: Boolean,
        val sensorTemperatureTooLow: Boolean,
        val sensorReadInterrupted: Boolean,
        val generalDeviceFault: Boolean,
        val timeFault: Boolean
    ) {
        companion object {
            fun fromBytes(bytes: UByteArray): SensorStatusAnnunciation? {
                if (bytes.size != 2) return null
                return SensorStatusAnnunciation(
                    deviceBatteryLow = bytes[0].bitFlagToBoolean(0),
                    sensorMalfunction = bytes[0].bitFlagToBoolean(1),
                    sampleSizeInsufficient = bytes[0].bitFlagToBoolean(2),
                    stripInsertionError = bytes[0].bitFlagToBoolean(3),
                    stripTypeIncorrect = bytes[0].bitFlagToBoolean(4),
                    sensorResultTooHigh = bytes[0].bitFlagToBoolean(5),
                    sensorResultTooLow = bytes[0].bitFlagToBoolean(6),
                    sensorTemperatureTooHigh = bytes[0].bitFlagToBoolean(7),
                    sensorTemperatureTooLow = bytes[1].bitFlagToBoolean(0),
                    sensorReadInterrupted = bytes[1].bitFlagToBoolean(1),
                    generalDeviceFault = bytes[1].bitFlagToBoolean(2),
                    timeFault = bytes[1].bitFlagToBoolean(3),
                )
            }
        }
    }

    companion object {
        @Suppress("UNUSED_VARIABLE")
        fun fromBytes(bytes: UByteArray): GlucoseMeasurementData? {
            val value = UByteQueue(bytes)
            if (value.size < 10) {
                return null
            }
            val flags: UByte = value.pop(1)?.first() ?: return null
            val flagTimeOffsetPresent: Boolean = flags.bitFlagToBoolean(0)
            val flagGlucoseMeasurementPresent: Boolean = flags.bitFlagToBoolean(1)
            val flagGlucoseUnit: Boolean = flags.bitFlagToBoolean(2)
            val flagSensorStatusAnnunciationPresent: Boolean = flags.bitFlagToBoolean(3)
            val flagContextInformationPresent: Boolean = flags.bitFlagToBoolean(4)

            val sequenceNumber: UShort = value.pop(2)?.toUShort(Endianness.LITTLE) ?: return null
            val baseTime: UByteArray = value.pop(7) ?: return null
            val dateTime: LocalDateTime = DateTimeData.fromBytes(baseTime)?.dateTime ?: return null

            var timeOffset: UShort? = null
            if (flagTimeOffsetPresent) {
                timeOffset = value.pop(2)?.toUShort(Endianness.LITTLE) ?: return null
            }

            var glucoseConcentration: Double? = null
            var unit: GlucoseMeasurementUnit? = null
            var type: GlucoseMeasurementType? = null
            var sampleLocation: GlucoseMeasurementSampleLocation? = null
            if (flagGlucoseMeasurementPresent) {
                unit = if (flagGlucoseUnit) GlucoseMeasurementUnit.MOL_PER_L else GlucoseMeasurementUnit.KG_PER_L
                val glucoseConcentrationShort =
                    value.pop(2)?.toUShort(Endianness.LITTLE)?.toShort() ?: return null
                glucoseConcentration = SFloat(
                    glucoseConcentrationShort,
                    offset = when (unit) {
                        GlucoseMeasurementUnit.KG_PER_L -> -5
                        GlucoseMeasurementUnit.MOL_PER_L -> -3
                    }
                ).toDouble()
                val typeLocationByte: UByte = value.pop(1)?.first() ?: return null
                val typeByte: UByte = (typeLocationByte.toInt() and 0x0F).toUByte()
                type = GlucoseMeasurementType.fromByte(typeByte) ?: return null
                val locationByte: UByte = (typeLocationByte.toInt() shr 4).toUByte()
                sampleLocation = GlucoseMeasurementSampleLocation.fromByte(locationByte) ?: return null
            }

            var sensorStatusAnnunciation: SensorStatusAnnunciation? = null
            if (flagSensorStatusAnnunciationPresent) {
                value.pop(2)?.let { bytes ->
                    sensorStatusAnnunciation = SensorStatusAnnunciation.fromBytes(
                        bytes.toUByteArray()
                    )
                }
            }

            return GlucoseMeasurementData(
                bytes,
                sequenceNumber,
                dateTime,
                timeOffset,
                glucoseConcentration,
                unit,
                type,
                sampleLocation,
                sensorStatusAnnunciation
            )
        }
    }
}
