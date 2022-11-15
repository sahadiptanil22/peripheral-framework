/*
 * ScanManager.kt
 * Android Peripheral Framework
 *
 * Copyright (C) 2022 Current Health. All rights reserved.
 */

package com.currenthealth.peripheralframework

import android.content.Context
import java.lang.ref.WeakReference

/**
 * Provider of all Bluetooth Low Energy scanning-related functionalities.
 */
interface ScanManager {
    /**
     * Obtain or set the delegate that will be informed of any events from [ScanManager]. The delegate
     * shall be maintained as a weak reference to avoid memory leaks in the event of circular
     * references.
     */
    var delegate: WeakReference<ScanManagerDelegate>?

    /**
     * Query if [ScanManager] is performing a BLE scan.
     */
    val isScanning: Boolean

    /**
     * Start a BLE scan with an optional set of criteria specified. Only one scan can be performed
     * at a time.
     *
     * Note: Unfiltered scans will likely require filtering at the plugin implementation level. It
     * is highly recommended to specify some scan criteria to prevent a flood of scan result
     * callbacks at the plugin level.
     */
    fun startScan(criteria: ScanCriteria?)

    /**
     * Stop an ongoing BLE scan.
     */
    fun stopScan()

    companion object {
        /**
         * Internally managed instance of [BleScanManager], exposed as a [ScanManager].
         */
        private lateinit var instance: ScanManager

        /**
         * Obtain the singleton instance of [ScanManager] managed by the Bluetooth package.
         */
        fun getInstance(context: Context): ScanManager {
            if (!::instance.isInitialized) {
                synchronized(this) {
                    instance = BleScanManager(context)
                }
            }
            return instance
        }
    }
}
