/*
 * BleExceptions.kt
 * Android Peripheral Framework
 *
 * Copyright (C) 2022 Current Health. All rights reserved.
 */

package com.currenthealth.peripheralframework.internal.bluetooth

import com.currenthealth.peripheralframework.PeripheralFramework
import java.util.UUID

internal class DeviceAlreadyBondedException(val device: Device) : Exception(
    "An operation has failed because ${device.macAddress} is already bonded with Android."
)

internal class DeviceNotBondedException(val device: Device) : Exception(
    "An operation has failed because ${device.macAddress} is not bonded with Android."
)

internal class DeviceAlreadyConnectedException(val device: Device) : Exception(
    "An operation has failed because the app is already connected to ${device.macAddress}."
)

internal class DeviceNotConnectedException(val device: Device) : Exception(
    "An operation has failed because the app is not connected to ${device.macAddress}."
)

internal class CharacteristicNotReadableException(val uuid: UUID) : Exception(
    "Attempting to read from characteristic $uuid that isn't readable!"
)

internal class CharacteristicNotWritableException(val uuid: UUID) : Exception(
    "Attempting to write to characteristic $uuid that isn't writable!"
)

internal class NotificationsNotSupportedException(val uuid: UUID) : Exception(
    "Characteristic $uuid does not support notifications and indications."
)

internal class BleSerialQueueException(pendingOperation: BleOperationType) : Exception(
    "Cannot execute next BLE operation when there is a pending operation $pendingOperation"
)

internal class ScannerNotAvailableException : Exception(
    "Cannot start BLE scan because BluetoothLeScanner is not available on this device."
)

/** Log this [Exception] to [PeripheralFramework.logger] and throw it. */
internal fun Exception.logAndThrow(): Nothing {
    PeripheralFramework.logger?.e(toString())
    throw this
}
