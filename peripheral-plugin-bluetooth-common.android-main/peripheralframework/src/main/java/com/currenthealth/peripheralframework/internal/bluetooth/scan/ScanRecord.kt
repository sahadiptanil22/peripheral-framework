/*
 * ScanRecord.kt
 * Android Peripheral Framework
 *
 * Copyright (C) 2022 Current Health. All rights reserved.
 */

package com.currenthealth.peripheralframework.internal.bluetooth.scan

import android.os.ParcelUuid
import android.util.SparseArray

/** Wrapper interface around [android.bluetooth.le.ScanRecord]. */
internal interface ScanRecord {
    val advertiseFlags: Int
    val rawBytes: ByteArray
    val deviceName: String?
    val manufacturerSpecificData: SparseArray<ByteArray>
    val serviceData: Map<ParcelUuid, ByteArray>
    val serviceSolicitationUuids: List<ParcelUuid>
    val serviceUuids: List<ParcelUuid>?
    val txPowerLevel: Int

    fun manufacturerSpecificData(manufacturerId: Int): ByteArray?
    fun serviceData(serviceDataUuid: ParcelUuid): ByteArray?
}
