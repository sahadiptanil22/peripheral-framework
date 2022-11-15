/*
 * UByteQueue.kt
 * Android Peripheral Framework
 *
 * Copyright (C) 2022 Current Health. All rights reserved.
 */

package com.currenthealth.peripheralframework.internal

internal class UByteQueue(private var bytes: UByteArray) {
    val size: Int
        get() = bytes.size

    fun pop(size: Int): UByteArray? {
        if (size > bytes.size) return null
        val toReturn = bytes.take(size).toUByteArray()
        bytes = bytes.drop(size).toUByteArray()
        return toReturn
    }
}
