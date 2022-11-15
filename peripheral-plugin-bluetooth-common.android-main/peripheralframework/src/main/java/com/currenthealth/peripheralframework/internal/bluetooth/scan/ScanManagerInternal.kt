/*
 * ScanManagerInternal.kt
 * Android Peripheral Framework
 *
 * Copyright (C) 2022 Current Health. All rights reserved.
 */

package com.currenthealth.peripheralframework.internal.bluetooth.scan

import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.content.Context
import com.currenthealth.peripheralframework.internal.bluetooth.ScannerNotAvailableException

/** An interface providing BLE scan functionalities. */
internal interface ScanManagerInternal {

    /** Indicate if a BLE scan is currently ongoing. */
    val isScanning: Boolean

    /** Register [listener] to be notified of any events surfaced by [ScanManagerInternal]. */
    fun registerListener(listener: ScanEventListener)

    /** Remove [listener] as an event listener, if it has previously been registered as one. */
    fun unregisterListener(listener: ScanEventListener)

    /**
     * Kick off a BLE scan using [filter] and [settings], with an optional minimum RSSI filtering.
     *
     * @throws [ScannerNotAvailableException] when Bluetooth LE scanner is not available on this
     * device.
     */
    @Throws(ScannerNotAvailableException::class)
    fun startScan(filter: List<ScanFilter>?, settings: ScanSettings, minRssi: Int? = null)

    /** Stop an ongoing BLE scan. */
    fun stopScan()

    companion object {
        /**
         * Internally managed instance of [BleScanManagerInternal], exposed as a [ScanManagerInternal].
         */
        private lateinit var instance: ScanManagerInternal

        /**
         * Obtain the singleton instance of [ScanManagerInternal] managed by the Bluetooth package.
         */
        fun getInstance(context: Context): ScanManagerInternal {
            if (!::instance.isInitialized) {
                synchronized(this) {
                    instance = BleScanManagerInternal(context)
                }
            }
            return instance
        }
    }
}
