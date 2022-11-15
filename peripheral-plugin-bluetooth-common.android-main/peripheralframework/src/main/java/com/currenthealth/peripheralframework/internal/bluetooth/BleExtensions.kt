/*
 * BleExtensions.kt
 * Android Peripheral Framework
 *
 * Copyright (C) 2022 Current Health. All rights reserved.
 */

package com.currenthealth.peripheralframework.internal.bluetooth

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import java.util.UUID

/** UUID of the Client Characteristic Configuration Descriptor (0x2902). */
internal const val CCC_DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805F9B34FB"

// BluetoothGatt

internal fun BluetoothGatt.findCharacteristic(
    characteristicUuid: UUID,
    serviceUuid: UUID
): BluetoothGattCharacteristic? {
    services?.firstOrNull { it.uuid == serviceUuid }
        ?.let { service ->
            service.characteristics?.firstOrNull { it.uuid == characteristicUuid }
                ?.let { matchingCharacteristic ->
                    return matchingCharacteristic
                }
        }
    return null
}

internal fun BluetoothGatt.findDescriptor(
    descriptorUuid: UUID,
    characteristicUuid: UUID,
    serviceUuid: UUID
): BluetoothGattDescriptor? {
    findCharacteristic(characteristicUuid, serviceUuid)?.let { characteristic ->
        characteristic.descriptors?.firstOrNull { it.uuid == descriptorUuid }
            ?.let { matchingDescriptor ->
                return matchingDescriptor
            }
    }
    return null
}

// BluetoothGattCharacteristic

internal fun BluetoothGattCharacteristic.printProperties(): String = mutableListOf<String>().apply {
    if (isReadable()) add("Readable")
    if (isWritable()) add("Writable")
    if (isWritableWithoutResponse()) add("Writable Without Response")
    if (isIndicatable()) add("Indicate")
    if (isNotifiable()) add("Notify")
    if (isEmpty()) add("<EMPTY>")
}.joinToString()

internal fun BluetoothGattCharacteristic.isReadable(): Boolean =
    containsProperty(BluetoothGattCharacteristic.PROPERTY_READ)

internal fun BluetoothGattCharacteristic.isWritable(): Boolean =
    containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE)

internal fun BluetoothGattCharacteristic.isWritableWithoutResponse(): Boolean =
    containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)

internal fun BluetoothGattCharacteristic.isIndicatable(): Boolean =
    containsProperty(BluetoothGattCharacteristic.PROPERTY_INDICATE)

internal fun BluetoothGattCharacteristic.isNotifiable(): Boolean =
    containsProperty(BluetoothGattCharacteristic.PROPERTY_NOTIFY)

internal fun BluetoothGattCharacteristic.containsProperty(property: Int): Boolean =
    properties and property != 0

// BluetoothGattDescriptor

internal fun BluetoothGattDescriptor.describeProperties(): String = mutableListOf<String>().apply {
    if (isReadable()) add("Readable")
    if (isWritable()) add("Writable")
    if (isEmpty()) add("<EMPTY>")
}.joinToString()

internal fun BluetoothGattDescriptor.isReadable(): Boolean =
    containsPermission(BluetoothGattDescriptor.PERMISSION_READ) ||
        containsPermission(BluetoothGattDescriptor.PERMISSION_READ_ENCRYPTED) ||
        containsPermission(BluetoothGattDescriptor.PERMISSION_READ_ENCRYPTED_MITM)

internal fun BluetoothGattDescriptor.isWritable(): Boolean =
    containsPermission(BluetoothGattDescriptor.PERMISSION_WRITE) ||
        containsPermission(BluetoothGattDescriptor.PERMISSION_WRITE_ENCRYPTED) ||
        containsPermission(BluetoothGattDescriptor.PERMISSION_WRITE_ENCRYPTED_MITM) ||
        containsPermission(BluetoothGattDescriptor.PERMISSION_WRITE_SIGNED) ||
        containsPermission(BluetoothGattDescriptor.PERMISSION_WRITE_SIGNED_MITM)

internal fun BluetoothGattDescriptor.containsPermission(permission: Int): Boolean =
    permissions and permission != 0

/**
 * Convenience extension function that returns true if this [BluetoothGattDescriptor]
 * is a Client Characteristic Configuration Descriptor.
 */
internal fun BluetoothGattDescriptor.isCccd() =
    uuid.toString().equals(CCC_DESCRIPTOR_UUID, ignoreCase = true)
