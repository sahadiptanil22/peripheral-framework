/*
 * ScanResult.kt
 * Android Peripheral Framework
 *
 * Copyright (C) 2022 Current Health. All rights reserved.
 */

package com.currenthealth.peripheralframework

import android.util.SparseArray
import java.util.Date
import java.util.UUID

interface ScanResult {
    /**
     * The handle of the underlying peripheral that can be used to establish a BLE connection to
     * the physical peripheral using [BluetoothManager].
     */
    val peripheral: BluetoothPeripheral

    /**
     * The most recently received signal strength indicator (RSSI) of the peripheral, in decibels.
     */
    val rssi: Int

    /**
     * A timestamp of when this scan result became available.
     */
    val timestamp: Date

    /**
     * The transmitting power level of the peripheral if provided in the advertising packet.
     */
    val txPower: Int?

    /**
     * Indicate whether the peripheral is advertising that it can be connected to.
     */
    val isConnectable: Boolean

    /**
     * Service UUIDs within the peripheral’s advertisement packet.
     */
    val serviceUUIDs: List<UUID>

    /**
     * A mapping between service UUIDs and their associated service data.
     */
    val serviceData: Map<UUID, ByteArray>

    /**
     * Custom data provided by the peripheral’s manufacturer.
     *
     * Note: Android supports multiple mappings of manufacturer IDs to associated data; iOS only
     * supports a single data entry without keying by manufacturer ID.
     */
    val manufacturerSpecificData: SparseArray<ByteArray>
}
