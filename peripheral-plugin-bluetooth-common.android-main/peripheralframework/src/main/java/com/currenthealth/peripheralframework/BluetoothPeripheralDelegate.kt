/*
 * BluetoothPeripheralDelegate.kt
 * Android Peripheral Framework
 *
 * Copyright (C) 2022 Current Health. All rights reserved.
 */

package com.currenthealth.peripheralframework

import java.util.UUID

interface BluetoothPeripheralDelegate {
    /**
     * [BluetoothPeripheral] is reporting the outcome of a request to update the peripheral’s ATT MTU
     * to a new value.
     */
    fun onMtuUpdated(
        peripheral: BluetoothPeripheral,
        mtu: Int,
        outcome: Outcome
    )

    /**
     * [BluetoothPeripheral] is reporting the outcome of a request to enable notifications or
     * indications (whichever is supported) on a characteristic.
     */
    fun onNotificationsEnabled(
        peripheral: BluetoothPeripheral,
        characteristicUuid: UUID,
        serviceUuid: UUID,
        outcome: Outcome
    )

    /**
     * [BluetoothPeripheral] is reporting the outcome of a request to disable notifications or
     * indications (whichever is supported) on a characteristic.
     */
    fun onNotificationsDisabled(
        peripheral: BluetoothPeripheral,
        characteristicUuid: UUID,
        serviceUuid: UUID,
        outcome: Outcome
    )

    /**
     * A characteristic’s value has changed, according to a read or notification/indication event.
     */
    fun onCharacteristicValueUpdated(
        peripheral: BluetoothPeripheral,
        characteristicUuid: UUID,
        serviceUuid: UUID,
        value: ByteArray,
        outcome: Outcome
    )

    /**
     * [BluetoothPeripheral] is reporting the outcome of a request to write to the value of a
     * characteristic.
     */
    fun onCharacteristicWrite(
        peripheral: BluetoothPeripheral,
        characteristicUuid: UUID,
        serviceUuid: UUID,
        value: ByteArray,
        outcome: Outcome
    )

    /**
     * [BluetoothPeripheral] is reporting the outcome of a request to write to the value of a
     * descriptor.
     */
    fun onDescriptorRead(
        peripheral: BluetoothPeripheral,
        descriptorUuid: UUID,
        characteristicUuid: UUID,
        serviceUuid: UUID,
        value: ByteArray,
        outcome: Outcome
    )

    /**
     * [BluetoothPeripheral] is reporting the outcome of a request to read the value of a descriptor.
     */
    fun onDescriptorWrite(
        peripheral: BluetoothPeripheral,
        descriptorUuid: UUID,
        characteristicUuid: UUID,
        serviceUuid: UUID,
        value: ByteArray,
        outcome: Outcome
    )
}
