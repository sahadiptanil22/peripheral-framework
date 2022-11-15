/*
 * BleDevice.kt
 * Android Peripheral Framework
 *
 * Copyright (C) 2022 Current Health. All rights reserved.
 */

package com.currenthealth.peripheralframework.internal.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.content.Context
import com.currenthealth.peripheralframework.PeripheralFramework
import kotlinx.parcelize.Parcelize

@Parcelize
@SuppressLint("MissingPermission") // App's role to ensure permissions are available
internal class BleDevice(private val device: BluetoothDevice) : Device {
    override val name: String?
        get() = device.name

    override val macAddress: String
        get() = device.address

    override val isBonded: Boolean
        get() = device.bondState == BluetoothDevice.BOND_BONDED

    override fun connect(
        context: Context,
        autoConnect: Boolean,
        callback: BluetoothGattCallback
    ): BluetoothGatt? = device.connectGatt(
        context.applicationContext,
        autoConnect,
        callback,
        BluetoothDevice.TRANSPORT_LE
    )

    override fun createBond() = device.createBond()

    override fun removeBond() {
        try {
            val method = device::class.java.getMethod("removeBond").invoke(device)
        } catch (e: Throwable) {
            PeripheralFramework.logger?.e("Failed to remove bond ${e.localizedMessage}")
        }
    }

    override fun equals(other: Any?) = macAddress == (other as? BleDevice)?.macAddress

    override fun hashCode() = macAddress.hashCode()
}
