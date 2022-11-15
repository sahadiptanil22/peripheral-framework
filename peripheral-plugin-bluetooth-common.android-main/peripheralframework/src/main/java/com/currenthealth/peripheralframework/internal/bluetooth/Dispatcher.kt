/*
 * Dispatcher.kt
 * Android Peripheral Framework
 *
 * Copyright (C) 2022 Current Health. All rights reserved.
 */

package com.currenthealth.peripheralframework.internal.bluetooth
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.content.Context
import com.currenthealth.peripheralframework.PeripheralFramework

/** Default ATT MTU which lets the ATT packet (inclusive of 3-byte header) fit in one LL packet. */
const val GATT_MIN_MTU_SIZE = 23
/** Maximum BLE ATT MTU size that Android will attempt to request, as defined in gatt_api.h. */
const val GATT_MAX_MTU_SIZE = 517

/**
 * An entry point interface for the BLE functionalities available to the app.
 *
 * [Dispatcher] queues operations and executes them serially in a FIFO fashion.
 *
 * Connection events are surfaced to [ConnectionEventListener] objects in a multicast fashion.
 * It is possible to have more than one [ConnectionEventListener] listening to events, but it's
 * good practice to call [unregisterListener] when event deliveries are no longer needed for a
 * listener.
 */
internal interface Dispatcher {
    /** Indicates if the Android Bluetooth adapter is available. */
    fun isBluetoothSupported(): Boolean

    /** Indicates if the Android Bluetooth adapter is available and turned on. */
    fun isBluetoothEnabled(): Boolean

    /** Returns a [Set] of [Device]s that are currently bonded with Android. */
    fun bondedDevices(): Set<Device>

    /** Indicates if [device] is connected via [Dispatcher]. */
    fun isDeviceConnected(device: Device): Boolean

    /** Get a [Connection] object for a connected [device]. */
    fun getConnection(device: Device): Connection?

    /** Get a [Device] object for the given Bluetooth hardware address. */
    fun getRemoteDevice(macAddress: String): Device?

    /**
     * Returns a [List] of [BluetoothGattService]s on [device], or null if [device] is not
     * connected.
     */
    fun servicesOnDevice(device: Device): List<BluetoothGattService>?

    /**
     * Indicates if [Dispatcher] is subscribed to notifications or indications on
     * [device]'s [characteristic].
     */
    fun isCharacteristicNotifying(
        device: Device,
        characteristic: BluetoothGattCharacteristic
    ): Boolean

    /**
     * Returns a set of currently notifying or indicating [BluetoothGattCharacteristic]s for a
     * given [Device].
     */
    fun notifyingCharacteristics(device: Device): Set<BluetoothGattCharacteristic>

    /** Register [listener] to be notified of any events surfaced by [Dispatcher]. */
    fun registerListener(listener: ConnectionEventListener)

    /** Removes [listener] as an event listener, if it has previously been registered as one. */
    fun unregisterListener(listener: ConnectionEventListener)

    /**
     * Attempts to establish a BLE bond with [device].
     *
     * @throws [DeviceAlreadyBondedException] when [device] is already bonded with Android.
     * @see [ConnectionEventListener.onBondCreationSucceeded]
     * @see [ConnectionEventListener.onBondCreationFailed]
     */
    @Throws(DeviceAlreadyBondedException::class)
    fun bond(device: Device)

    /**
     * Attempts to remove a BLE bond with [device]. This action does not have any associated
     * callbacks because it utilizes a private method to achieve this functionality.
     *
     * @throws [DeviceNotBondedException] when [device] is not currently bonded with Android.
     */
    fun removeBond(device: Device)

    /**
     * Initiate a connection with [device] and perform service discovery.
     *
     * @throws [DeviceAlreadyConnectedException] when [device] is already in a connected state.
     * @see [ConnectionEventListener.onConnectionSetupComplete]
     */
    @Throws(DeviceAlreadyConnectedException::class)
    fun connect(device: Device)

    /**
     * Perform teardown on the GATT connection for [device]. Any operations that are running or
     * pending in the queue for [device] will be removed.
     *
     * @see [ConnectionEventListener.onDisconnect]
     */
    fun disconnect(device: Device)

    /**
     * Cancel an ongoing connection attempt for [device]. Any operations that are running or
     * pending in the queue for [device] will be removed.
     *
     * @throws [DeviceAlreadyConnectedException] when [device] is already in a connected state.
     */
    @Throws(DeviceAlreadyConnectedException::class)
    fun cancelConnectionAttempt(device: Device)

    /**
     * Perform a read on [BluetoothGattCharacteristic], if it's readable.
     *
     * @throws [DeviceNotConnectedException] when [device] is not in a connected state.
     * @throws [CharacteristicNotReadableException] when [characteristic] is not readable.
     * @see [ConnectionEventListener.onCharacteristicRead]
     */
    @Throws(DeviceNotConnectedException::class, CharacteristicNotReadableException::class)
    fun readCharacteristic(device: Device, characteristic: BluetoothGattCharacteristic)

    /**
     * Perform a write on [BluetoothGattCharacteristic], if it's writable, using [writeType].
     *
     * @throws [DeviceNotConnectedException] when [device] is not in a connected state.
     * @throws [CharacteristicNotWritableException] when [characteristic] is not writable.
     * @see [ConnectionEventListener.onCharacteristicWrite]
     */
    @Throws(DeviceNotConnectedException::class, CharacteristicNotWritableException::class)
    fun writeCharacteristic(
        device: Device,
        characteristic: BluetoothGattCharacteristic,
        payload: ByteArray,
        writeType: CharacteristicWriteTypeInternal = CharacteristicWriteTypeInternal.DEFAULT
    )

    /**
     * Perform a read on [BluetoothGattDescriptor]. If the descriptor is not readable, the
     * [ConnectionEventListener.onDescriptorRead] callback will have an outcome of type
     * [BleOperationOutcome.Failure] and a status code of [BluetoothGatt.GATT_READ_NOT_PERMITTED].
     *
     * @throws [DeviceNotConnectedException] when [device] is not in a connected state.
     * @see [ConnectionEventListener.onDescriptorRead]
     */
    @Throws(DeviceNotConnectedException::class)
    fun readDescriptor(device: Device, descriptor: BluetoothGattDescriptor)

    /**
     * Perform a write on [BluetoothGattDescriptor]. If the descriptor is not writable, the
     * [ConnectionEventListener.onDescriptorWrite] callback will have an outcome of type
     * [BleOperationOutcome.Failure] and a status code of [BluetoothGatt.GATT_WRITE_NOT_PERMITTED].
     *
     * @throws [DeviceNotConnectedException] when [device] is not in a connected state.
     * @see [ConnectionEventListener.onDescriptorWrite]
     */
    @Throws(DeviceNotConnectedException::class)
    fun writeDescriptor(
        device: Device,
        descriptor: BluetoothGattDescriptor,
        payload: ByteArray
    )

    /**
     * Enable notifications or indications on [characteristic], if it can be subscribed to.
     *
     * [Dispatcher] checks to see if the [BluetoothGattCharacteristic] supports either
     * indications or notifications, and validates the presence of the Client Characteristic
     * Configuration Descriptor (CCCD).
     *
     * @throws [DeviceNotConnectedException] when [device] is not in a connected state.
     * @throws [NotificationsNotSupportedException] when [characteristic] does not support
     * notifications or indications.
     * @see [ConnectionEventListener.onNotificationsEnabled]
     * @see [ConnectionEventListener.onCharacteristicChanged]
     */
    @Throws(DeviceNotConnectedException::class, NotificationsNotSupportedException::class)
    fun enableNotifications(device: Device, characteristic: BluetoothGattCharacteristic)

    /**
     * Disable notifications or indications on [characteristic].
     *
     * @throws [DeviceNotConnectedException] when [device] is not in a connected state.
     * @throws [NotificationsNotSupportedException] when [characteristic] does not support
     * notifications or indications.
     * @see [ConnectionEventListener.onNotificationsDisabled]
     */
    @Throws(DeviceNotConnectedException::class, NotificationsNotSupportedException::class)
    fun disableNotifications(device: Device, characteristic: BluetoothGattCharacteristic)

    /**
     * Request an ATT MTU update of [mtu].
     *
     * This kicks off a negotiation process between Android and the [Device]'s firmware,
     * and the final ATT MTU value may be different from what was requested in [mtu].
     *
     * @throws [DeviceNotConnectedException] when [device] is not in a connected state.
     * @see [ConnectionEventListener.onMtuChanged]
     */
    @Throws(DeviceNotConnectedException::class)
    fun requestMtu(device: Device, mtu: Int)

    companion object {
        /**
         * Internally managed instance of [BleDispatcher], exposed as a [Dispatcher].
         */
        private lateinit var instance: Dispatcher

        /**
         * Obtain the singleton instance of [Dispatcher] managed by the Bluetooth package.
         */
        fun getInstance(context: Context): Dispatcher {
            if (!::instance.isInitialized) {
                synchronized(this) {
                    instance = BleDispatcher(context)
                }
            }
            return instance
        }
    }
}

/**
 * State of the current Android device's Bluetooth adapter.
 */
enum class BluetoothState {
    /** Bluetooth is off. */
    OFF,
    /** Bluetooth is turning off. */
    TURNING_OFF,
    /** Bluetooth is turning on. */
    TURNING_ON,
    /** Bluetooth is on. */
    ON,
    /** Bluetooth state is unknown. */
    UNKNOWN;

    /** Textual description of the Bluetooth state. */
    fun description() = when (this) {
        OFF -> "OFF"
        TURNING_OFF -> "TURNING OFF"
        TURNING_ON -> "TURNING ON"
        ON -> "ON"
        UNKNOWN -> "UNKNOWN"
    }

    companion object {
        fun fromBluetoothAdapterState(status: Int) = when (status) {
            BluetoothAdapter.STATE_OFF -> OFF
            BluetoothAdapter.STATE_TURNING_OFF -> TURNING_OFF
            BluetoothAdapter.STATE_TURNING_ON -> TURNING_ON
            BluetoothAdapter.STATE_ON -> ON
            else -> {
                PeripheralFramework.logger?.e("Unexpected Android BluetoothAdapter state of $status!")
                UNKNOWN
            }
        }
    }
}

/**
 * BLE bond state description of a [Device].
 */
enum class BondState {
    /**
     * [Device] is bonded to Android.
     *
     * @see [BluetoothDevice.BOND_BONDED]
     */
    BONDED,
    /**
     * [Device] is bonding with Android.
     *
     * @see [BluetoothDevice.BOND_BONDING]
     */
    BONDING,
    /**
     * [Device] is not bonded to Android.
     *
     * @see [BluetoothDevice.BOND_NONE]
     */
    NONE;

    /** Textual description of the bond state. */
    fun description() = when (this) {
        BONDED -> "BONDED"
        BONDING -> "BONDING"
        NONE -> "NOT BONDED"
    }

    companion object {
        fun fromBluetoothDeviceBondStatus(status: Int) = when (status) {
            BluetoothDevice.BOND_BONDED -> BONDED
            BluetoothDevice.BOND_BONDING -> BONDING
            BluetoothDevice.BOND_NONE -> NONE
            else -> {
                PeripheralFramework.logger?.e("Unexpected Android BluetoothDevice bond state of $status!")
                NONE
            }
        }
    }
}
