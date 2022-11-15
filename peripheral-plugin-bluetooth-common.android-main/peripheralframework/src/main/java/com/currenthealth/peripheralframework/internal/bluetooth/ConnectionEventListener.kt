/*
 * ConnectionEventListener.kt
 * Android Peripheral Framework
 *
 * Copyright (C) 2022 Current Health. All rights reserved.
 */

package com.currenthealth.peripheralframework.internal.bluetooth

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor

/* ktlint-disable parameter-list-wrapping */
/**
 * A listener containing callback methods to be registered with [Dispatcher]. Override
 * only the event callbacks that you're interested in getting notified about.
 *
 * Register a listener entity by calling [Dispatcher.registerListener], and unregister
 * using [Dispatcher.unregisterListener] when event deliveries are no longer needed.
 */
internal class ConnectionEventListener(internal val identifier: String) {
    /** A [Device] is connected, and service discovery has completed successfully. */
    var onConnectionSetupComplete: ((connection: Connection) -> Unit)? = null

    /** A [Device] has been disconnected from Android. */
    var onDisconnect: ((device: Device) -> Unit)? = null

    /** A [BluetoothGattCharacteristic] value read has finished executing for a [Device]. */
    var onCharacteristicRead: (
        (
            device: Device,
            characteristic: BluetoothGattCharacteristic,
            outcome: BleOperationOutcome
        ) -> Unit
    )? = null

    /** A [BluetoothGattCharacteristic] value write has finished executing for a [Device]. */
    var onCharacteristicWrite: (
        (
            device: Device,
            characteristic: BluetoothGattCharacteristic,
            outcome: BleOperationOutcome
        ) -> Unit
    )? = null

    /** A [BluetoothGattDescriptor] value read has finished executing for a [Device]. */
    var onDescriptorRead: (
        (
            device: Device,
            descriptor: BluetoothGattDescriptor,
            outcome: BleOperationOutcome
        ) -> Unit
    )? = null

    /** A [BluetoothGattDescriptor] value write has finished executing for a [Device]. */
    var onDescriptorWrite: (
        (
            device: Device,
            descriptor: BluetoothGattDescriptor,
            outcome: BleOperationOutcome
        ) -> Unit
    )? = null

    /** A [BluetoothGattCharacteristic] value change has occurred for a [Device]. */
    var onCharacteristicChanged: (
        (
            device: Device,
            characteristic: BluetoothGattCharacteristic
        ) -> Unit
    )? = null

    /**
     * Notifications or indications, whichever is supported, has been enabled on the
     * [BluetoothGattCharacteristic] for a [Device].
     */
    var onNotificationsEnabled: (
        (
            device: Device,
            characteristic: BluetoothGattCharacteristic
        ) -> Unit
    )? = null

    /**
     * Notifications or indications, whichever is supported, has been disabled on the
     * [BluetoothGattCharacteristic] for a [Device].
     */
    var onNotificationsDisabled: (
        (
            device: Device,
            characteristic: BluetoothGattCharacteristic
        ) -> Unit
    )? = null

    /** A [Device]'s ATT MTU value has changed. */
    var onMtuChanged: (
        (
            device: Device,
            mtu: Int,
            outcome: BleOperationOutcome
        ) -> Unit
    )? = null

    /**
     * A [Device]'s BLE bond state has changed. Note that even devices that aren't managed by the
     * app will have their bond state changes surfaced, so it's highly recommended to check the
     * [Device] delivered to ensure it's the device you're interested in.
     */
    var onBondStateChanged: (
        (
            device: Device,
            oldState: BondState,
            newState: BondState
        ) -> Unit
    )? = null

    /** Android has successfully bonded with a [Device] as a result of a [CreateBond] operation. */
    var onBondCreationSucceeded: ((device: Device) -> Unit)? = null

    /** Android has failed to bond with a [Device] as a result of a [CreateBond] operation. */
    var onBondCreationFailed: ((device: Device) -> Unit)? = null

    /** The current Android device's Bluetooth adapter state has changed. */
    var onBluetoothStateChanged: (
        (
            oldState: BluetoothState,
            newState: BluetoothState
        ) -> Unit
    )? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ConnectionEventListener

        return identifier == other.identifier
    }

    override fun hashCode(): Int {
        return identifier.hashCode()
    }
}
