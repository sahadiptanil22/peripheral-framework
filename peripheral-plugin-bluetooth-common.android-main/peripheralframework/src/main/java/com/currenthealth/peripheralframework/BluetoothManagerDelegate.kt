/*
 * BluetoothManagerDelegate.kt
 * Android Peripheral Framework
 *
 * Copyright (C) 2022 Current Health. All rights reserved.
 */

package com.currenthealth.peripheralframework

interface BluetoothManagerDelegate {
    /**
     * [BluetoothManager] is reporting a change in the mobile device’s Bluetooth radio status.
     *
     * Note: Most BLE operations can only be performed when the mobile device's Bluetooth radio
     * status is [BluetoothStatus.ON].
     */
    fun onBluetoothStatusChanged(bluetoothStatus: BluetoothStatus)

    /**
     * [BluetoothManager] is reporting the outcome of a request to connect to a peripheral. In the
     * success case, this means [BluetoothManager] has successfully connected to a peripheral and
     * discovered all its services and characteristics — the peripheral is ready to be interacted
     * with.
     */
    fun onConnectionSetupComplete(peripheral: BluetoothPeripheral, outcome: Outcome)

    /**
     * [BluetoothManager] has disconnected unexpectedly from a peripheral or is reporting the outcome
     * of a request to disconnect from a peripheral.
     */
    fun onDisconnect(peripheral: BluetoothPeripheral, outcome: Outcome)
}
