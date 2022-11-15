/*
 * ByteArrayExtensions.kt
 * Android Peripheral Framework
 *
 * Copyright (C) 2022 Current Health. All rights reserved.
 */

package com.currenthealth.peripheralframework.internal.extensions

internal fun ByteArray.toHexString(
    separator: String = " ",
    prefix: String = "",
    individualPrefix: String = ""
): String =
    joinToString(separator = separator, prefix = prefix) { String.format("$individualPrefix%02X", it) }
