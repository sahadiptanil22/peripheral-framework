package com.currenthealth.peripheralframework

import com.currenthealth.peripheralframework.internal.bluetooth.CharacteristicNotReadableException
import com.currenthealth.peripheralframework.internal.bluetooth.CharacteristicNotWritableException
import com.currenthealth.peripheralframework.internal.bluetooth.CharacteristicWriteTypeInternal
import com.currenthealth.peripheralframework.internal.bluetooth.ConnectionEventListener
import com.currenthealth.peripheralframework.internal.bluetooth.Device
import com.currenthealth.peripheralframework.internal.bluetooth.DeviceNotConnectedException
import com.currenthealth.peripheralframework.internal.bluetooth.Dispatcher
import com.currenthealth.peripheralframework.internal.bluetooth.NotificationsNotSupportedException
import java.lang.ref.WeakReference
import java.util.UUID

internal class BluetoothPeripheralImpl(internal val device: Device) : BluetoothPeripheral {
    private val dispatcher = Dispatcher.getInstance(PeripheralFramework.applicationContext)

    override var delegate: WeakReference<BluetoothPeripheralDelegate>? = null
    override val name: String?
        get() = device.name
    override val macAddress: String
        get() = device.macAddress
    override val isConnected: Boolean
        get() = dispatcher.isDeviceConnected(device)

    override fun requestMtu(mtu: Int) {
        try {
            dispatcher.requestMtu(device, mtu)
        } catch (e: DeviceNotConnectedException) {
            delegate?.get()?.onMtuUpdated(
                this,
                0,
                Outcome.Failure(FailureReason.PERIPHERAL_DISCONNECTED)
            )
        }
    }

    override fun writeToCharacteristic(
        uuid: UUID,
        serviceUuid: UUID,
        data: ByteArray,
        writeType: CharacteristicWriteType
    ) {
        val characteristic =
            dispatcher.getConnection(device)?.getCharacteristic(uuid, serviceUuid) ?: run {
                delegate?.get()?.onCharacteristicWrite(
                    this,
                    uuid,
                    serviceUuid,
                    ByteArray(0),
                    Outcome.Failure(FailureReason.RESOURCE_UNAVAILABLE)
                )
                return
            }
        try {
            dispatcher.writeCharacteristic(
                device,
                characteristic,
                data,
                CharacteristicWriteTypeInternal.fromCharacteristicWriteType(writeType)
            )
        } catch (e: DeviceNotConnectedException) {
            delegate?.get()?.onCharacteristicWrite(
                this,
                uuid,
                serviceUuid,
                ByteArray(0),
                Outcome.Failure(FailureReason.PERIPHERAL_DISCONNECTED)
            )
        } catch (e: CharacteristicNotWritableException) {
            delegate?.get()?.onCharacteristicWrite(
                this,
                uuid,
                serviceUuid,
                ByteArray(0),
                Outcome.Failure(FailureReason.NOT_ALLOWED)
            )
        }
    }

    override fun readFromCharacteristic(uuid: UUID, serviceUuid: UUID) {
        val characteristic =
            dispatcher.getConnection(device)?.getCharacteristic(uuid, serviceUuid) ?: run {
                delegate?.get()?.onCharacteristicValueUpdated(
                    this,
                    uuid,
                    serviceUuid,
                    ByteArray(0),
                    Outcome.Failure(FailureReason.RESOURCE_UNAVAILABLE)
                )
                return
            }
        try {
            dispatcher.readCharacteristic(
                device,
                characteristic
            )
        } catch (e: DeviceNotConnectedException) {
            delegate?.get()?.onCharacteristicValueUpdated(
                this,
                uuid,
                serviceUuid,
                ByteArray(0),
                Outcome.Failure(FailureReason.PERIPHERAL_DISCONNECTED)
            )
        } catch (e: CharacteristicNotReadableException) {
            delegate?.get()?.onCharacteristicValueUpdated(
                this,
                uuid,
                serviceUuid,
                ByteArray(0),
                Outcome.Failure(FailureReason.NOT_ALLOWED)
            )
        }
    }

    override fun writeToDescriptor(
        uuid: UUID,
        characteristicUuid: UUID,
        serviceUuid: UUID,
        data: ByteArray
    ) {
        val descriptor =
            dispatcher.getConnection(device)?.getDescriptor(uuid, characteristicUuid, serviceUuid)
                ?: run {
                    delegate?.get()?.onDescriptorWrite(
                        this,
                        uuid,
                        characteristicUuid,
                        serviceUuid,
                        ByteArray(0),
                        Outcome.Failure(FailureReason.RESOURCE_UNAVAILABLE)
                    )
                    return
                }
        try {
            dispatcher.writeDescriptor(
                this.device,
                descriptor,
                data
            )
        } catch (e: DeviceNotConnectedException) {
            delegate?.get()?.onDescriptorWrite(
                this,
                uuid,
                characteristicUuid,
                serviceUuid,
                ByteArray(0),
                Outcome.Failure(FailureReason.PERIPHERAL_DISCONNECTED)
            )
        }
    }

    override fun readFromDescriptor(uuid: UUID, characteristicUuid: UUID, serviceUuid: UUID) {
        val descriptor =
            dispatcher.getConnection(device)?.getDescriptor(uuid, characteristicUuid, serviceUuid)
                ?: run {
                    delegate?.get()?.onDescriptorRead(
                        this,
                        uuid,
                        characteristicUuid,
                        serviceUuid,
                        ByteArray(0),
                        Outcome.Failure(FailureReason.RESOURCE_UNAVAILABLE)
                    )
                    return
                }
        try {
            dispatcher.readDescriptor(
                this.device,
                descriptor
            )
        } catch (e: DeviceNotConnectedException) {
            delegate?.get()?.onDescriptorRead(
                this,
                uuid,
                characteristicUuid,
                serviceUuid,
                ByteArray(0),
                Outcome.Failure(FailureReason.PERIPHERAL_DISCONNECTED)
            )
        }
    }

    override fun enableNotifications(uuid: UUID, serviceUuid: UUID) {
        val characteristic =
            dispatcher.getConnection(device)?.getCharacteristic(uuid, serviceUuid) ?: run {
                delegate?.get()?.onNotificationsEnabled(
                    this,
                    uuid,
                    serviceUuid,
                    Outcome.Failure(FailureReason.RESOURCE_UNAVAILABLE)
                )
                return
            }
        try {
            dispatcher.enableNotifications(
                device,
                characteristic,
            )
        } catch (e: DeviceNotConnectedException) {
            delegate?.get()?.onNotificationsEnabled(
                this,
                uuid,
                serviceUuid,
                Outcome.Failure(FailureReason.PERIPHERAL_DISCONNECTED)
            )
        } catch (e: NotificationsNotSupportedException) {
            delegate?.get()?.onNotificationsEnabled(
                this,
                uuid,
                serviceUuid,
                Outcome.Failure(FailureReason.NOT_ALLOWED)
            )
        }
    }

    override fun disableNotifications(uuid: UUID, serviceUuid: UUID) {
        val characteristic =
            dispatcher.getConnection(device)?.getCharacteristic(uuid, serviceUuid) ?: run {
                delegate?.get()?.onNotificationsDisabled(
                    this,
                    uuid,
                    serviceUuid,
                    Outcome.Failure(FailureReason.RESOURCE_UNAVAILABLE)
                )
                return
            }
        try {
            dispatcher.disableNotifications(
                device,
                characteristic,
            )
        } catch (e: DeviceNotConnectedException) {
            delegate?.get()?.onNotificationsDisabled(
                this,
                uuid,
                serviceUuid,
                Outcome.Failure(FailureReason.PERIPHERAL_DISCONNECTED)
            )
        } catch (e: NotificationsNotSupportedException) {
            delegate?.get()?.onNotificationsDisabled(
                this,
                uuid,
                serviceUuid,
                Outcome.Failure(FailureReason.NOT_ALLOWED)
            )
        }
    }

    private val eventListener = ConnectionEventListener(
        "com.currenthealth.peripheralframework.BluetoothPeripheralImpl"
    ).apply {
        dispatcher.registerListener(this)
        onCharacteristicRead = { device, characteristic, outcome ->
            if (device.macAddress == this@BluetoothPeripheralImpl.device.macAddress) {
                try {
                    delegate?.get()?.onCharacteristicValueUpdated(
                        this@BluetoothPeripheralImpl,
                        characteristic.uuid,
                        characteristic.service.uuid,
                        characteristic.value,
                        Outcome.fromBleOperationOutcome(outcome)
                    )
                } catch (e: NullPointerException) {
                    delegate?.get()?.onCharacteristicValueUpdated(
                        this@BluetoothPeripheralImpl,
                        characteristic.uuid,
                        characteristic.service.uuid,
                        ByteArray(0),
                        Outcome.fromBleOperationOutcome(outcome)
                    )
                }
            }
        }

        onCharacteristicWrite = { device, characteristic, outcome ->
            if (device.macAddress == this@BluetoothPeripheralImpl.device.macAddress) {
                try {
                    delegate?.get()?.onCharacteristicWrite(
                        this@BluetoothPeripheralImpl,
                        characteristic.uuid,
                        characteristic.service.uuid,
                        characteristic.value,
                        Outcome.fromBleOperationOutcome(outcome)
                    )
                } catch (e: NullPointerException) {
                    delegate?.get()?.onCharacteristicWrite(
                        this@BluetoothPeripheralImpl,
                        characteristic.uuid,
                        characteristic.service.uuid,
                        ByteArray(0),
                        Outcome.fromBleOperationOutcome(outcome)
                    )
                }
            }
        }

        onDescriptorRead = { device, descriptor, outcome ->
            if (device.macAddress == this@BluetoothPeripheralImpl.device.macAddress) {
                delegate?.get()?.onDescriptorRead(
                    this@BluetoothPeripheralImpl,
                    descriptor.uuid,
                    descriptor.characteristic.uuid,
                    descriptor.characteristic.service.uuid,
                    descriptor.value,
                    Outcome.fromBleOperationOutcome(outcome)
                )
            }
        }

        onDescriptorWrite = { device, descriptor, outcome ->
            if (device.macAddress == this@BluetoothPeripheralImpl.device.macAddress) {
                delegate?.get()?.onDescriptorWrite(
                    this@BluetoothPeripheralImpl,
                    descriptor.uuid,
                    descriptor.characteristic.uuid,
                    descriptor.characteristic.service.uuid,
                    descriptor.value,
                    Outcome.fromBleOperationOutcome(outcome)
                )
            }
        }

        onCharacteristicChanged = { device, characteristic ->
            if (device.macAddress == this@BluetoothPeripheralImpl.device.macAddress) {
                delegate?.get()?.onCharacteristicValueUpdated(
                    this@BluetoothPeripheralImpl,
                    characteristic.uuid,
                    characteristic.service.uuid,
                    characteristic.value,
                    Outcome.Success
                )
            }
        }

        onNotificationsEnabled = { device, characteristic ->
            if (device.macAddress == this@BluetoothPeripheralImpl.device.macAddress) {
                delegate?.get()?.onNotificationsEnabled(
                    this@BluetoothPeripheralImpl,
                    characteristic.uuid,
                    characteristic.service.uuid,
                    Outcome.Success
                )
            }
        }

        onNotificationsDisabled = { device, characteristic ->
            if (device.macAddress == this@BluetoothPeripheralImpl.device.macAddress) {
                delegate?.get()?.onNotificationsDisabled(
                    this@BluetoothPeripheralImpl,
                    characteristic.uuid,
                    characteristic.service.uuid,
                    Outcome.Success
                )
            }
        }

        onMtuChanged = { device, mtu, outcome ->
            if (device.macAddress == this@BluetoothPeripheralImpl.device.macAddress) {
                delegate?.get()?.onMtuUpdated(
                    this@BluetoothPeripheralImpl,
                    mtu,
                    Outcome.fromBleOperationOutcome(outcome)
                )
            }
        }
    }
}
