/*
 * BluetoothPeripheral.kt
 * Android Peripheral Framework
 *
 * Copyright (C) 2022 Current Health. All rights reserved.
 */

package com.currenthealth.peripheralframework

import java.lang.ref.WeakReference
import java.util.UUID

interface BluetoothPeripheral {
    /**
     * Obtain or set the delegate that will be informed of any events from
     * [BluetoothPeripheralDelegate]. The delegate shall be maintained as a weak reference to
     * avoid memory leaks due to circular references.
     */
    var delegate: WeakReference<BluetoothPeripheralDelegate>?

    /**
     * The Bluetooth local name of the peripheral.
     */
    val name: String?

    /**
     * The MAC address of the Bluetooth peripheral.
     */
    val macAddress: String

    /**
     * Indicate whether the peripheral is currently connected with the mobile device over BLE.
     */
    val isConnected: Boolean

    /**
     * Attempt to negotiate with the peripheral for a specific ATT MTU to be used for the current
     * connection. Note that the resulting ATT MTU may not always be equal to the requested value.
     */
    fun requestMtu(mtu: Int)

    /**
     * Write a value to a given characteristic using the specified [CharacteristicWriteType].
     *
     * Note: The maximum allowed length of a data payload that can be sent without being chunked up
     * internally by the mobile platforms is affected by the ATT MTU of the connection. Developers
     * should request for the MTU that they need, and the corresponding callback will inform them
     * on the actual negotiated MTU;
     */
    fun writeToCharacteristic(uuid: UUID, serviceUuid: UUID, data: ByteArray, writeType: CharacteristicWriteType)

    /**
     * Read the value of a given characteristic.
     *
     * Note: The maximum allowed length of a data payload that can be sent without being chunked up
     * internally by the mobile platforms is affected by the ATT MTU, similar to a characteristic
     * write operation.
     */
    fun readFromCharacteristic(uuid: UUID, serviceUuid: UUID)

    /**
     * Write a value to a given descriptor.
     */
    fun writeToDescriptor(uuid: UUID, characteristicUuid: UUID, serviceUuid: UUID, data: ByteArray)

    /**
     * Read the value of a given descriptor.
     */
    fun readFromDescriptor(uuid: UUID, characteristicUuid: UUID, serviceUuid: UUID)

    /**
     * Enable notifications or indications (whichever is supported) on a given characteristic.
     */
    fun enableNotifications(uuid: UUID, serviceUuid: UUID)

    /**
     * Disable notifications or indications (whichever is supported) on a given characteristic.
     */
    fun disableNotifications(uuid: UUID, serviceUuid: UUID)
}
