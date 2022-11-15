/*
 * UUIDExtensions.kt
 * Android Peripheral Framework
 *
 * Copyright (C) 2022 Current Health. All rights reserved.
 */

package com.currenthealth.peripheralframework.internal.extensions

import java.util.UUID

private const val BASE_BLUETOOTH_UUID_POSTFIX = "0000-1000-8000-00805F9B34FB"

 internal fun uuidFromShortCode16(shortCode16: String): UUID? {
    if (shortCode16.length != 4) return null
    return UUID.fromString("0000${shortCode16.uppercase()}-$BASE_BLUETOOTH_UUID_POSTFIX")
}

 internal fun uuidFromShortCode32(shortCode32: String): UUID? {
    if (shortCode32.length != 8) return null
    return UUID.fromString("${shortCode32.uppercase()}-$BASE_BLUETOOTH_UUID_POSTFIX")
}
