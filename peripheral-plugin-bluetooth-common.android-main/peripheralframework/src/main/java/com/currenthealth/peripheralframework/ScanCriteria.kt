/*
 * ScanCriteria.kt
 * Android Peripheral Framework
 *
 * Copyright (C) 2022 Current Health. All rights reserved.
 */

package com.currenthealth.peripheralframework

import java.util.UUID

/**
 * A Kotlin data class that is initialized with optional properties, each representing a scan
 * criterion. Together, this combination of criteria describes the peripheral that a plugin is
 * interested in.
 *
 * All of the properties are defined as optional (nullable) types. If no criterion is specified,
 * this implicitly implies that no filtering should be performed on BLE scan results.
 */
data class ScanCriteria(
    /**
     * Bluetooth local name of the peripheral of interest.
     */
    val deviceName: String? = null,

    /**
     * The MAC address of the peripheral of interest which is assumed to be a Random Static Address
     * that is ideally fixed for the lifetime of the peripheral.
     */
    val macAddress: String? = null,

    /**
     * Advertised service UUIDs of the peripheral of interest.
     */
    val serviceUuids: List<UUID>? = null,

    /**
     * The minimum RSSI signal strength (in dBm as a negative number) that a peripheral must be
     * advertising at before it can be considered (usually together with some other criteria) a
     * peripheral of interest.
     */
    val rssiThreshold: Int? = null,

    /**
     * The manufacturer data (iOS), or manufacturer ID and its associated manufacturer-specific
     * data (Android) that — when matched exactly — identifies a peripheral of interest.
     */
    val manufacturerSpecificData: Map<Int, ByteArray>? = null,

    /**
     * The service UUID and its associated service data that — when matched exactly — identifies a
     * peripheral of interest.
     */
    val serviceData: Map<UUID, ByteArray>? = null
)
