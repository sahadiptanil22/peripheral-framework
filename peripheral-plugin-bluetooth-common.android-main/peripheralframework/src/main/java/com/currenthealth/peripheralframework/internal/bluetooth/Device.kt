/*
 * Device.kt
 * Android Peripheral Framework
 *
 * Copyright (C) 2022 Current Health. All rights reserved.
 */

package com.currenthealth.peripheralframework.internal.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.content.Context
import android.os.Parcelable

/**
 * A representation of a remote Bluetooth Low Energy (BLE) peripheral. This interface is essentially
 * a wrapper over [BluetoothDevice].
 *
 * @see [BluetoothDevice] for documentation.
 */
internal interface Device : Parcelable {
    val name: String?
    val macAddress: String
    val isBonded: Boolean

    /** See [BluetoothDevice.connectGatt] */
    fun connect(
        context: Context,
        autoConnect: Boolean,
        callback: BluetoothGattCallback
    ): BluetoothGatt?

    /** See [BluetoothDevice.createBond] */
    fun createBond(): Boolean

    /**
     * Utilize a private `removeBond` method on [BluetoothDevice] to remove the BLE bond between
     * this [Device] and Android.
     */
    fun removeBond()
}
