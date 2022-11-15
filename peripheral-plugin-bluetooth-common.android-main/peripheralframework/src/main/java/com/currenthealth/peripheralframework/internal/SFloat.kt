/*
 * SFloat.kt
 * Android Peripheral Framework
 *
 * Copyright (C) 2022 Current Health. All rights reserved.
 */

package com.currenthealth.peripheralframework.internal

import kotlin.experimental.and
import kotlin.experimental.or

/**
 * Class representing IEEE 11073-20601 16-bit floating point data type

 */
internal data class SFloat(private val value: Short, private val offset: Int = 0) {
    fun toDouble(): Double {
        return when (value) {
            0x07FF.toShort() -> {
                Double.NaN
            }
            0x0800.toShort() -> {
                Double.NaN
            }
            0x07FE.toShort() -> {
                Double.POSITIVE_INFINITY
            }
            0x0802.toShort() -> {
                Double.NEGATIVE_INFINITY
            }
            0x0801.toShort() -> {
                Double.NaN
            }
            else -> {
                val exponent: Short = (
                    offset.toShort() +
                        (
                            if (value < 0) {
                                ((value.toInt() shr 12).toShort() and 0x0F or 0xF0)
                            } else {
                                (value.toInt() shr 12).toShort() and 0x0F
                            }
                            )
                    ).toShort()
                val mantissa: Short =
                    if ((value and 0x0800) != 0.toShort()) { // if mantissa should be negative
                        ((value and 0x0FFF) or 0xF000.toShort())
                    } else {
                        (value and 0x0FFF.toShort())
                    }

                mantissa.toDouble() * Math.pow(10.0, exponent.toDouble())
            }
        }
    }
}
