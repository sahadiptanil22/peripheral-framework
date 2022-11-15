/*
 * UByteExtensions.kt
 * Android Peripheral Framework
 *
 * Copyright (C) 2022 Current Health. All rights reserved.
 */

package com.currenthealth.peripheralframework.internal.extensions

internal fun UByte.bitFlagToBoolean(index: Int): Boolean {
    val bit: Int = when (index) {
        0 -> this.toInt() and 0b00000001
        1 -> (this.toInt() shr 1) and 0b00000001
        2 -> (this.toInt() shr 2) and 0b00000001
        3 -> (this.toInt() shr 3) and 0b00000001
        4 -> (this.toInt() shr 4) and 0b00000001
        5 -> (this.toInt() shr 5) and 0b00000001
        6 -> (this.toInt() shr 6) and 0b00000001
        7 -> (this.toInt() shr 7) and 0b00000001
        else -> {
            return false
        }
    }
    return bit != 0
}
