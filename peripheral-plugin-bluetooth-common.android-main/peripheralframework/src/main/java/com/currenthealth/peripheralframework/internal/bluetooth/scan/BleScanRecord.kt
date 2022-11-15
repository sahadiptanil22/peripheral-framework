/*
 * BleScanRecord.kt
 * Android Peripheral Framework
 *
 * Copyright (C) 2022 Current Health. All rights reserved.
 */

package com.currenthealth.peripheralframework.internal.bluetooth.scan

import android.os.Build
import android.os.ParcelUuid
import android.util.SparseArray

internal class BleScanRecord(private val record: android.bluetooth.le.ScanRecord) : ScanRecord {

    override val advertiseFlags: Int
        get() = record.advertiseFlags

    override val rawBytes: ByteArray
        get() = record.bytes

    override val deviceName: String?
        get() = record.deviceName

    override val manufacturerSpecificData: SparseArray<ByteArray>
        get() = record.manufacturerSpecificData ?: SparseArray()

    override val serviceData: Map<ParcelUuid, ByteArray>
        get() = record.serviceData

    override val serviceSolicitationUuids: List<ParcelUuid>
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            record.serviceSolicitationUuids
        } else {
            listOf()
        }

    override val serviceUuids: List<ParcelUuid>?
        get() = record.serviceUuids

    override val txPowerLevel: Int
        get() = record.txPowerLevel

    override fun manufacturerSpecificData(manufacturerId: Int) =
        record.getManufacturerSpecificData(manufacturerId)

    override fun serviceData(serviceDataUuid: ParcelUuid) =
        record.getServiceData(serviceDataUuid)
}
