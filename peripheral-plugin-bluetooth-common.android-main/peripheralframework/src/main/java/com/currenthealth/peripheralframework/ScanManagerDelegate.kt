/*
 * ScanManagerDelegate.kt
 * Android Peripheral Framework
 *
 * Copyright (C) 2022 Current Health. All rights reserved.
 */

package com.currenthealth.peripheralframework

interface ScanManagerDelegate {
    /**
     * [ScanManager] is reporting the outcome of a request to start a BLE scan using a [ScanCriteria]
     * that was provided earlier.
     */
    fun onScanStarted(criteria: ScanCriteria?, outcome: Outcome)

    /**
     * [ScanManager] is reporting the outcome of a request to stop a BLE scan using a [ScanCriteria]
     * that was provided earlier.
     */
    fun onScanStopped(criteria: ScanCriteria?, outcome: Outcome)

    /**
     * [ScanManager] is reporting a BLE scan result.
     *
     * Note: This callback can be reported multiple times for each peripheral in the vicinity that
     * matches the [ScanCriteria] used for a given scan.
     */
    fun onScanResult(scanResult: ScanResult)
}
