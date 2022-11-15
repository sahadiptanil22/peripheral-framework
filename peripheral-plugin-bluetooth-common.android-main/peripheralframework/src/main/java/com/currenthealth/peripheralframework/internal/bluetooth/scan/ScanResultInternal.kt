/*
 * ScanResultInternal.kt
 * Android Peripheral Framework
 *
 * Copyright (C) 2022 Current Health. All rights reserved.
 */

package com.currenthealth.peripheralframework.internal.bluetooth.scan

import android.os.Parcelable
import com.currenthealth.peripheralframework.internal.bluetooth.Device

/** Wrapper interface around [android.bluetooth.le.ScanResult]. */
internal interface ScanResultInternal : Parcelable {
    val device: Device
    val rssi: Int
    val timestampNanos: Long
    val txPower: Int
    val scanRecord: ScanRecord?
    val isConnectable: Boolean
}
