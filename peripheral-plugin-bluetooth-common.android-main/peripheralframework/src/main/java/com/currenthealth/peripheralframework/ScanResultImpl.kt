package com.currenthealth.peripheralframework

import android.util.SparseArray
import java.util.Date
import java.util.UUID

data class ScanResultImpl(
    override val peripheral: BluetoothPeripheral,
    override val rssi: Int,
    override val timestamp: Date,
    override val txPower: Int?,
    override val isConnectable: Boolean,
    override val serviceUUIDs: List<UUID>,
    override val serviceData: Map<UUID, ByteArray>,
    override val manufacturerSpecificData: SparseArray<ByteArray>
) : ScanResult
