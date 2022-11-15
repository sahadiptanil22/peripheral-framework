/*
 * UByteArrayExtensions.kt
 * Android Peripheral Framework
 *
 * Copyright (C) 2022 Current Health. All rights reserved.
 */

package com.currenthealth.peripheralframework.internal.extensions

import java.nio.ByteBuffer
import java.nio.ByteOrder

internal fun UByteArray.toUShort(receivedEndianness: Endianness = Endianness.LITTLE): UShort {
    val byteArray = this.toByteArray()
    return when (receivedEndianness) {
        Endianness.BIG -> {
            ByteBuffer.wrap(byteArray).order(ByteOrder.BIG_ENDIAN).short.toUShort()
        }
        Endianness.LITTLE -> {
            ByteBuffer.wrap(byteArray).order(ByteOrder.LITTLE_ENDIAN).short.toUShort()
        }
    }
}

internal fun UByteArray.toUInt(receivedEndianness: Endianness = Endianness.LITTLE): UInt {
    val byteArray = this.toByteArray()
    return when (receivedEndianness) {
        Endianness.BIG -> {
            ByteBuffer.wrap(byteArray).order(ByteOrder.BIG_ENDIAN).int.toUInt()
        }
        Endianness.LITTLE -> {
            ByteBuffer.wrap(byteArray).order(ByteOrder.LITTLE_ENDIAN).int.toUInt()
        }
    }
}

internal enum class Endianness {
    LITTLE,
    BIG
}
