/*
 * BleConnection.kt
 * Android Peripheral Framework
 *
 * Copyright (C) 2022 Current Health. All rights reserved.
 */

package com.currenthealth.peripheralframework.internal.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import java.util.UUID

@SuppressLint("MissingPermission") // App's role to ensure permissions are available
internal class BleConnection(private val gatt: BluetoothGatt) : Connection {
    override val device: Device = BleDevice(gatt.device)

    override val services: List<BluetoothGattService>
        get() = gatt.services

    override fun cancelConnectionAttempt() = gatt.disconnect()

    override fun close() = gatt.close()

    override fun discoverServices() = gatt.discoverServices()

    override fun requestMtu(mtu: Int) = gatt.requestMtu(mtu)

    override fun getService(uuid: UUID): BluetoothGattService? = gatt.getService(uuid)

    override fun getCharacteristic(uuid: UUID, serviceUuid: UUID) = gatt.findCharacteristic(uuid, serviceUuid)

    override fun getDescriptor(uuid: UUID, characteristicUuid: UUID, serviceUuid: UUID) = gatt.findDescriptor(uuid, characteristicUuid, serviceUuid)

    override fun setCharacteristicNotification(
        characteristic: BluetoothGattCharacteristic,
        enable: Boolean
    ) = gatt.setCharacteristicNotification(characteristic, enable)

    override fun readCharacteristic(characteristic: BluetoothGattCharacteristic) =
        gatt.readCharacteristic(characteristic)

    override fun readDescriptor(descriptor: BluetoothGattDescriptor) =
        gatt.readDescriptor(descriptor)

    override fun writeCharacteristic(characteristic: BluetoothGattCharacteristic) =
        gatt.writeCharacteristic(characteristic)

    override fun writeDescriptor(descriptor: BluetoothGattDescriptor) =
        gatt.writeDescriptor(descriptor)
}
