/*
 * BleScanResultInternal.kt
 * Android Peripheral Framework
 *
 * Copyright (C) 2022 Current Health. All rights reserved.
 */

package com.currenthealth.peripheralframework.internal.bluetooth.scan

import com.currenthealth.peripheralframework.internal.bluetooth.BleDevice
import com.currenthealth.peripheralframework.internal.bluetooth.Device
import kotlinx.parcelize.Parcelize

@Parcelize
internal class BleScanResultInternal(
    private val result: android.bluetooth.le.ScanResult
) : ScanResultInternal {
    override val device: Device
        get() = BleDevice(result.device)

    override val rssi: Int
        get() = result.rssi

    override val timestampNanos: Long
        get() = result.timestampNanos

    override val txPower: Int
        get() = result.txPower

    override val scanRecord: ScanRecord?
        get() = result.scanRecord?.let {
            BleScanRecord(it)
        }

    override val isConnectable: Boolean
        get() = result.isConnectable
}
