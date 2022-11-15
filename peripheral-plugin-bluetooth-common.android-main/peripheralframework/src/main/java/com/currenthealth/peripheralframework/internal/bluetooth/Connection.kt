/*
 * Connection.kt
 * Android Peripheral Framework
 *
 * Copyright (C) 2022 Current Health. All rights reserved.
 */

package com.currenthealth.peripheralframework.internal.bluetooth

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import java.util.UUID

/**
 * A representation of a remote Bluetooth Low Energy (BLE) connection. This interface is essentially
 * a wrapper over [BluetoothGatt].
 *
 * @see [BluetoothGatt] for documentation.
 */
internal interface Connection {
    val device: Device
    val services: List<BluetoothGattService>

    fun cancelConnectionAttempt()
    fun close()
    fun discoverServices(): Boolean
    fun requestMtu(mtu: Int): Boolean
    fun getService(uuid: UUID): BluetoothGattService?
    fun getCharacteristic(uuid: UUID, serviceUuid: UUID): BluetoothGattCharacteristic?
    fun getDescriptor(uuid: UUID, characteristicUuid: UUID, serviceUuid: UUID): BluetoothGattDescriptor?

    fun setCharacteristicNotification(
        characteristic: BluetoothGattCharacteristic,
        enable: Boolean
    ): Boolean

    fun readCharacteristic(characteristic: BluetoothGattCharacteristic): Boolean
    fun readDescriptor(descriptor: BluetoothGattDescriptor): Boolean

    fun writeCharacteristic(characteristic: BluetoothGattCharacteristic): Boolean
    fun writeDescriptor(descriptor: BluetoothGattDescriptor): Boolean
}
