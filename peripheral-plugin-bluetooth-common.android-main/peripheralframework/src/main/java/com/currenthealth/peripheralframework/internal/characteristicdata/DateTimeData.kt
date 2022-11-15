/*
 * DateTimeData.kt
 * Android Peripheral Framework
 *
 * Copyright (C) 2022 Current Health. All rights reserved.
 */

package com.currenthealth.peripheralframework.internal.characteristicdata

import com.currenthealth.peripheralframework.internal.extensions.Endianness
import com.currenthealth.peripheralframework.internal.extensions.toUShort
import java.time.LocalDateTime

/**
 * Parses Data from Standard Bluetooth GATT Characteristic, Date Time
 * based on GATT Specification Supplement V5, section 3.64:
 * https://www.bluetooth.com/specifications/specs/gatt-specification-supplement-5/
 */
data class DateTimeData(
    val dateTime: LocalDateTime
) {
    companion object {
        fun fromBytes(value: UByteArray): DateTimeData? {
            if (value.size != 7) return null
            val year: UShort = value.sliceArray(0 until 2).toUShort(Endianness.LITTLE)
            if (year < 1582u || year > 9999u) return null
            val month: UByte = value[2]
            if (month < 1u || month > 12u) return null
            val day: UByte = value[3]
            if (day < 1u || day > 31u) return null
            val hour: UByte = value[4]
            if (hour > 23u) return null
            val minute: UByte = value[5]
            if (minute > 59u) return null
            val second: UByte = value[6]
            if (second > 59u) return null

            return DateTimeData(
                LocalDateTime.of(
                    year.toInt(),
                    month.toInt(),
                    day.toInt(),
                    hour.toInt(),
                    minute.toInt(),
                    second.toInt()
                )
            )
        }
    }
}
