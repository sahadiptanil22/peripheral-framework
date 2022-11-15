/*
 * ScanEventListener.kt
 * Android Peripheral Framework
 *
 * Copyright (C) 2022 Current Health. All rights reserved.
 */

package com.currenthealth.peripheralframework.internal.bluetooth.scan

/**
 * A listener containing callback methods to be registered with [ScanManagerInternal]. Override
 * only the event callbacks that you're interested in getting notified about.
 *
 * Register a listener entity by calling [ScanManagerInternal.registerListener], and unregister
 * using [ScanManagerInternal.unregisterListener] when event deliveries are no longer needed.
 */
internal class ScanEventListener {
    /** A [ScanResultInternal] has been found via BLE scan. */
    var onScanResult: ((result: ScanResultInternal) -> Unit)? = null

    /** BLE scan has failed due to an error. */
    var onScanFailed: ((errorCode: Int) -> Unit)? = null
}
