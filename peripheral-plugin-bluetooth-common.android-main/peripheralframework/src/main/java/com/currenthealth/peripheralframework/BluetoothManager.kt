/*
 * BluetoothManager.kt
 * Android Peripheral Framework
 *
 * Copyright (C) 2022 Current Health. All rights reserved.
 */

package com.currenthealth.peripheralframework

import android.content.Context
import java.lang.ref.WeakReference

interface BluetoothManager {
    /**
     * Obtain or set the delegate that will be informed of any events from [BluetoothManager]. The
     * delegate shall be maintained as a weak reference to avoid memory leaks due to circular
     * references.
     */
    var delegate: WeakReference<BluetoothManagerDelegate>?

    /**
     * Obtain the current state of the mobile device’s Bluetooth radio.
     */
    val bluetoothStatus: BluetoothStatus

    /**
     * Instruct [BluetoothManager] to asynchronously connect to a peripheral over BLE and discover
     * all services and characteristics that are available on the peripheral.
     */
    fun connect(peripheral: BluetoothPeripheral)

    /**
     * Instruct [BluetoothManager] to asynchronously disconnect from a peripheral over BLE.
     */
    fun disconnect(peripheral: BluetoothPeripheral)

    /**
     * Attempt to obtain a [BluetoothPeripheral] handle matching the provided UUID or MAC address
     * without starting a BLE scan.
     */
    fun getPeripheral(macAddress: String): BluetoothPeripheral?

    companion object {
        /**
         * Internally managed instance of [BluetoothManagerImpl], exposed as a [BluetoothManager].
         */
        private lateinit var instance: BluetoothManager

        /**
         * Obtain the singleton instance of [BluetoothManager] managed by the Bluetooth package.
         */
        fun getInstance(context: Context): BluetoothManager {
            if (!::instance.isInitialized) {
                synchronized(this) {
                    instance = BluetoothManagerImpl(context)
                }
            }
            return instance
        }
    }
}
