/*
 * BleOperationType.kt
 * Android Peripheral Framework
 *
 * Copyright (C) 2022 Current Health. All rights reserved.
 */

package com.currenthealth.peripheralframework.internal.bluetooth

import java.util.UUID

/** Abstract sealed interface representing a type of BLE operation. */
internal sealed interface BleOperationType {
    val device: Device
}

/** Connect to [device] and perform service discovery. */
internal data class Connect(override val device: Device) : BleOperationType

/** Disconnect from [device] and release all connection resources. */
internal data class Disconnect(override val device: Device) : BleOperationType

/** Write [payload] as the value of a characteristic represented by [characteristicUuid]. */
internal data class CharacteristicWrite(
    override val device: Device,
    val characteristicUuid: UUID,
    val serviceUuid: UUID,
    val writeType: Int,
    val payload: ByteArray
) : BleOperationType {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CharacteristicWrite

        if (device != other.device) return false
        if (characteristicUuid != other.characteristicUuid) return false
        if (serviceUuid != other.serviceUuid) return false
        if (writeType != other.writeType) return false
        if (!payload.contentEquals(other.payload)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = device.hashCode()
        result = 31 * result + characteristicUuid.hashCode()
        result = 31 * result + writeType
        result = 31 * result + payload.contentHashCode()
        return result
    }
}

/** Read the value of a characteristic represented by [characteristicUuid]. */
internal data class CharacteristicRead(
    override val device: Device,
    val characteristicUuid: UUID,
    val serviceUuid: UUID
) : BleOperationType

/** Write [payload] as the value of a descriptor represented by [descriptorUuid]. */
internal data class DescriptorWrite(
    override val device: Device,
    val descriptorUuid: UUID,
    val characteristicUuid: UUID,
    val serviceUuid: UUID,
    val payload: ByteArray
) : BleOperationType {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DescriptorWrite

        if (device != other.device) return false
        if (descriptorUuid != other.descriptorUuid) return false
        if (characteristicUuid != other.characteristicUuid) return false
        if (serviceUuid != other.serviceUuid) return false
        if (!payload.contentEquals(other.payload)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = device.hashCode()
        result = 31 * result + descriptorUuid.hashCode()
        result = 31 * result + payload.contentHashCode()
        return result
    }
}

/** Read the value of a descriptor represented by [descriptorUuid]. */
internal data class DescriptorRead(
    override val device: Device,
    val descriptorUuid: UUID,
    val characteristicUuid: UUID,
    val serviceUuid: UUID
) : BleOperationType

/** Enable notifications/indications on a characteristic represented by [characteristicUuid]. */
internal data class EnableNotifications(
    override val device: Device,
    val characteristicUuid: UUID,
    val serviceUuid: UUID
) : BleOperationType

/** Disable notifications/indications on a characteristic represented by [characteristicUuid]. */
internal data class DisableNotifications(
    override val device: Device,
    val characteristicUuid: UUID,
    val serviceUuid: UUID
) : BleOperationType

/** Request for an ATT MTU of [mtu]. */
internal data class MtuRequest(
    override val device: Device,
    val mtu: Int
) : BleOperationType

/** Start the bonding (pairing) process with [device]. */
internal data class CreateBond(override val device: Device) : BleOperationType

/** Attempt to remove the BLE bond with [device]. */
internal data class RemoveBond(override val device: Device) : BleOperationType
