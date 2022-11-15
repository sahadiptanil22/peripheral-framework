/*
 * BleDispatcher.kt
 * Android Peripheral Framework
 *
 * Copyright (C) 2022 Current Health. All rights reserved.
 */

package com.currenthealth.peripheralframework.internal.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.currenthealth.peripheralframework.PeripheralFramework
import com.currenthealth.peripheralframework.internal.extensions.toHexString
import java.lang.ref.WeakReference
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Concrete implementation of [Dispatcher] that interacts with the Android BLE APIs
 * in order to perform actions such as connects, disconnects, reads and writes.
 *
 * The public function overrides perform error checking before enqueuing an operation to the
 * serial queue [operationQueue].
 */
@SuppressLint("MissingPermission") // App's role to ensure permissions are available
internal class BleDispatcher(
    private val appContext: Context,
) : Dispatcher {

    /** [ConnectionEventListener]s registered to get notified of events. */
    private var listeners: MutableSet<WeakReference<ConnectionEventListener>> = mutableSetOf()
    /** The BLE operation that is currently pending. */
    private var pendingOperation: BleOperationType? = null

    /** A mapping between [Device]s and any pending [Connection]. */
    private val pendingConnectionMap = ConcurrentHashMap<Device, Connection>()
    /** A mapping between connected [Device]s and their [Connection]s. */
    private val deviceConnectionMap = ConcurrentHashMap<Device, Connection>()
    /** A mapping between [Device]s and notifying [BluetoothGattCharacteristic]s. */
    private val notifyingCharacteristicsMap =
        ConcurrentHashMap<Device, Set<BluetoothGattCharacteristic>>()
    /** Serial queue of BLE operations to be executed in a FIFO manner. */
    private val operationQueue = ConcurrentLinkedQueue<BleOperationType>()
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val bluetoothManager = appContext.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        bluetoothManager?.adapter
    }
    private val androidDeviceDescription =
        "${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE})"

    // - PUBLIC functions

    override fun isBluetoothSupported(): Boolean =
        bluetoothAdapter != null

    override fun isBluetoothEnabled(): Boolean =
        bluetoothAdapter?.isEnabled == true

    override fun bondedDevices(): Set<Device> =
        bluetoothAdapter?.bondedDevices?.filter { device ->
            device.type != BluetoothDevice.DEVICE_TYPE_CLASSIC
        }?.map { BleDevice(it) }?.toSet() ?: setOf()

    override fun isDeviceConnected(device: Device) =
        deviceConnectionMap.containsKey(device)

    override fun getConnection(device: Device): Connection? =
        deviceConnectionMap[device]

    override fun getRemoteDevice(macAddress: String): Device? {
        return bluetoothAdapter?.getRemoteDevice(macAddress)?.let {
            BleDevice(it)
        }
    }

    override fun servicesOnDevice(device: Device): List<BluetoothGattService>? =
        deviceConnectionMap[device]?.services

    override fun isCharacteristicNotifying(
        device: Device,
        characteristic: BluetoothGattCharacteristic
    ) = notifyingCharacteristicsMap[device]?.contains(characteristic) ?: false

    override fun notifyingCharacteristics(device: Device) =
        notifyingCharacteristicsMap[device] ?: setOf()

    override fun registerListener(listener: ConnectionEventListener) {
        if (listeners.any { it.get()?.identifier == listener.identifier }) {
            PeripheralFramework.logger?.w("${listener.identifier} is already registered with Dispatcher, replacing")
            listeners = listeners.filter {
                it.get()?.identifier != listener.identifier
            }.toMutableSet()
        }
        listeners.add(WeakReference(listener))
        listeners = listeners.filter { it.get() != null }.toMutableSet()
        PeripheralFramework.logger?.d("Added listener $listener, ${listeners.size} listeners total")
    }

    override fun unregisterListener(listener: ConnectionEventListener) {
        // Removing elements while in a loop results in a java.util.ConcurrentModificationException
        var toRemove: WeakReference<ConnectionEventListener>? = null
        listeners.forEach {
            if (it.get() == listener) {
                toRemove = it
            }
        }
        toRemove?.let {
            listeners.remove(it)
            PeripheralFramework.logger?.d("Removed listener ${it.get()}, ${listeners.size} listeners total")
        }
    }

    override fun bond(device: Device) {
        if (device.isBonded) {
            PeripheralFramework.logger?.e("Already bonded with ${device.macAddress} (${device.name})")
            DeviceAlreadyBondedException(device).logAndThrow()
        } else {
            CreateBond(device).also { enqueueOperation(it) }
        }
    }

    override fun removeBond(device: Device) {
        if (!device.isBonded) {
            PeripheralFramework.logger?.e("Not currently bonded with ${device.macAddress} (${device.name})")
            DeviceNotBondedException(device).logAndThrow()
        } else {
            RemoveBond(device).also { enqueueOperation(it) }
        }
    }

    override fun connect(device: Device) {
        if (device.isConnected()) {
            PeripheralFramework.logger?.e("Cannot connect to ${device.macAddress} (${device.name}): already connected")
            DeviceAlreadyConnectedException(device).logAndThrow()
        } else {
            Connect(device).also { enqueueOperation(it) }
        }
    }

    override fun disconnect(device: Device) {
        // Remove all operations belonging to this BluetoothDevice before enqueuing Disconnect
        if (pendingOperation?.device == device) {
            PeripheralFramework.logger?.w("Clearing out pending operation $pendingOperation due to disconnect")
            pendingOperation = null
        }

        purgeOperationsForDevice(device)

        PeripheralFramework.logger?.w("Disconnecting from ${device.macAddress} (${device.name})")
        Disconnect(device).also { enqueueOperation(it) }
    }

    override fun cancelConnectionAttempt(device: Device) {
        if (device.isConnected()) {
            PeripheralFramework.logger?.e("Cannot cancel connection attempt for ${device.macAddress} (${device.name}): already connected")
            DeviceAlreadyConnectedException(device).logAndThrow()
        } else {
            pendingConnectionMap[device]?.let {
                it.cancelConnectionAttempt()
                it.close()
            }
            pendingConnectionMap.remove(device)
            purgeOperationsForDevice(device)

            if (pendingOperation is Connect) {
                signalEndOfOperation()
            }
        }
    }

    override fun readCharacteristic(device: Device, characteristic: BluetoothGattCharacteristic) {
        when {
            device.isNotConnected() -> DeviceNotConnectedException(device).logAndThrow()
            !characteristic.isReadable() -> CharacteristicNotReadableException(characteristic.uuid)
                .logAndThrow()
            device.isConnected() && characteristic.isReadable() -> {
                CharacteristicRead(
                    device,
                    characteristic.uuid,
                    characteristic.service.uuid
                ).also { enqueueOperation(it) }
            }
        }
    }

    override fun writeCharacteristic(
        device: Device,
        characteristic: BluetoothGattCharacteristic,
        payload: ByteArray,
        writeType: CharacteristicWriteTypeInternal
    ) {
        if (device.isNotConnected()) {
            DeviceNotConnectedException(device).logAndThrow()
        }

        val writeTypeToUse = when (writeType) {
            CharacteristicWriteTypeInternal.DEFAULT -> when {
                characteristic.isWritable() -> BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                characteristic.isWritableWithoutResponse() -> {
                    BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                }
                else -> CharacteristicNotWritableException(characteristic.uuid).logAndThrow()
            }
            CharacteristicWriteTypeInternal.WITH_RESPONSE -> {
                if (characteristic.isWritable()) {
                    BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                } else {
                    CharacteristicNotWritableException(characteristic.uuid).logAndThrow()
                }
            }
            CharacteristicWriteTypeInternal.WITHOUT_RESPONSE -> {
                if (characteristic.isWritableWithoutResponse()) {
                    BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                } else {
                    CharacteristicNotWritableException(characteristic.uuid).logAndThrow()
                }
            }
        }

        // Characteristic is writable and we're connected to the device
        CharacteristicWrite(
            device,
            characteristic.uuid,
            characteristic.service.uuid,
            writeTypeToUse,
            payload
        ).also {
            enqueueOperation(it)
        }
    }

    override fun readDescriptor(device: Device, descriptor: BluetoothGattDescriptor) {
        if (device.isNotConnected()) {
            DeviceNotConnectedException(device).logAndThrow()
        } else {
            DescriptorRead(
                device,
                descriptor.uuid,
                descriptor.characteristic.uuid,
                descriptor.characteristic.service.uuid
            ).also { enqueueOperation(it) }
        }
    }

    override fun writeDescriptor(
        device: Device,
        descriptor: BluetoothGattDescriptor,
        payload: ByteArray
    ) {
        if (device.isNotConnected()) {
            DeviceNotConnectedException(device).logAndThrow()
        } else {
            DescriptorWrite(
                device,
                descriptor.uuid,
                descriptor.characteristic.uuid,
                descriptor.characteristic.service.uuid,
                payload
            ).also { enqueueOperation(it) }
        }
    }

    override fun enableNotifications(
        device: Device,
        characteristic: BluetoothGattCharacteristic
    ) {
        when {
            device.isNotConnected() -> DeviceNotConnectedException(device).logAndThrow()
            !characteristic.isIndicatable() && !characteristic.isNotifiable() -> {
                NotificationsNotSupportedException(characteristic.uuid).logAndThrow()
            }
            device.isConnected() && (characteristic.isIndicatable() || characteristic.isNotifiable()) -> {
                EnableNotifications(device, characteristic.uuid, characteristic.service.uuid).also { enqueueOperation(it) }
            }
        }
    }

    override fun disableNotifications(
        device: Device,
        characteristic: BluetoothGattCharacteristic
    ) {
        when {
            device.isNotConnected() -> DeviceNotConnectedException(device).logAndThrow()
            !characteristic.isIndicatable() && !characteristic.isNotifiable() -> {
                NotificationsNotSupportedException(characteristic.uuid).logAndThrow()
            }
            device.isConnected() && (characteristic.isIndicatable() || characteristic.isNotifiable()) -> {
                DisableNotifications(device, characteristic.uuid, characteristic.service.uuid).also { enqueueOperation(it) }
            }
        }
    }

    override fun requestMtu(device: Device, mtu: Int) {
        if (device.isConnected()) {
            val normalizedMtu = mtu.coerceIn(GATT_MIN_MTU_SIZE, GATT_MAX_MTU_SIZE)
            MtuRequest(device, normalizedMtu).also { enqueueOperation(it) }
        } else {
            DeviceNotConnectedException(device).logAndThrow()
        }
    }

    // - Beginning of PRIVATE functions

    private fun Device.isConnected() = isDeviceConnected(this)
    private fun Device.isNotConnected() = !isDeviceConnected(this)

    @Synchronized
    private fun enqueueOperation(operation: BleOperationType) {
        PeripheralFramework.logger?.d("Enqueuing $operation")
        operationQueue.add(operation)
        if (pendingOperation == null) {
            doNextOperation()
        }
    }

    @Synchronized
    private fun signalEndOfOperation() {
        PeripheralFramework.logger?.d("End of $pendingOperation")
        pendingOperation = null
        if (operationQueue.isNotEmpty()) {
            doNextOperation()
        }
    }

    @Synchronized
    private fun purgeOperationsForDevice(device: Device) {
        val outstandingOperations = operationQueue.toTypedArray()
        if (outstandingOperations.any { it.device == device }) {
            PeripheralFramework.logger?.w("Removing operations belonging to ${device.macAddress} (${device.name})")
            val otherOperations = outstandingOperations.filter { it.device != device }
            while (operationQueue.isNotEmpty()) { operationQueue.poll() }
            operationQueue.addAll(otherOperations)
        }
    }

    /**
     * Perform the next operation in [operationQueue]. All permission checks should be performed
     * before an operation can be enqueued by [enqueueOperation].
     *
     * The only times when this function can and should be called are:
     * 1. An operation has been added to the queue, but there is no pending operation.
     * 2. An operation has finished executing, and the queue is not empty.
     */
    @Synchronized
    private fun doNextOperation() {
        pendingOperation?.let {
            BleSerialQueueException(it).logAndThrow()
        }

        val operation = operationQueue.poll() ?: run {
            // This is not an error, it merely means we ran out of tasks to perform
            PeripheralFramework.logger?.d("Operation queue empty, nothing to perform.")
            return
        }

        pendingOperation = operation

        // Handle Connect separately from other operations that require device to be connected
        if (operation is Connect) {
            with(operation) {
                if (device.isNotConnected()) {
                    PeripheralFramework.logger?.w("Connecting to ${device.macAddress} (${device.name})")
                    device.connect(appContext, false, callback)?.let { gatt ->
                        val connection = BleConnection(gatt)
                        pendingConnectionMap[device] = connection
                    }
                } else {
                    PeripheralFramework.logger?.e("${device.macAddress} (${device.name}) is already connected!")
                    signalEndOfOperation()
                }
            }
            return
        } else if (operation is CreateBond) {
            with(operation) {
                if (device.isBonded) {
                    PeripheralFramework.logger?.e("${device.macAddress} (${device.name}) is already bonded!")
                    listeners.forEach {
                        it.get()?.onBondCreationFailed?.invoke(device)
                    }
                    signalEndOfOperation()
                } else {
                    PeripheralFramework.logger?.w("Bonding with ${device.macAddress} (${device.name})")
                    if (!device.createBond()) {
                        PeripheralFramework.logger?.e("Bonding with ${device.macAddress} (${device.name}) failed!")
                        listeners.forEach {
                            it.get()?.onBondCreationFailed?.invoke(device)
                        }
                        signalEndOfOperation()
                    }
                }
            }
            return
        } else if (operation is RemoveBond) {
            operation.device.removeBond()
            signalEndOfOperation()
            return
        }

        // Check Connection availability for other operations
        val connection = deviceConnectionMap[operation.device]
            ?: this@BleDispatcher.run {
                PeripheralFramework.logger?.e("Not connected to ${operation.device.macAddress}! Aborting $operation.")
                signalEndOfOperation()
                return
            }

        when (operation) {
            is Disconnect -> with(operation) {
                PeripheralFramework.logger?.w("$androidDeviceDescription disconnecting from ${device.macAddress}")
                connection.close()
                deviceConnectionMap.remove(device)
                notifyingCharacteristicsMap.remove(device)
                listeners.forEach { it.get()?.onDisconnect?.invoke(device) }
                signalEndOfOperation()
                PeripheralFramework.logger?.w("$androidDeviceDescription disconnected from ${device.macAddress} (${device.name})")
            }
            is CharacteristicWrite -> with(operation) {
                connection.getCharacteristic(characteristicUuid, serviceUuid)?.let { characteristic ->
                    PeripheralFramework.logger?.w("Writing to characteristic $characteristicUuid: ${payload.toHexString()}")
                    characteristic.writeType = writeType
                    characteristic.value = payload
                    connection.writeCharacteristic(characteristic)
                } ?: this@BleDispatcher.run {
                    PeripheralFramework.logger?.e("Cannot find $characteristicUuid to write to")
                    signalEndOfOperation()
                }
            }
            is CharacteristicRead -> with(operation) {
                connection.getCharacteristic(characteristicUuid, serviceUuid)?.let { characteristic ->
                    PeripheralFramework.logger?.w("Reading from characteristic $characteristicUuid")
                    connection.readCharacteristic(characteristic)
                } ?: this@BleDispatcher.run {
                    PeripheralFramework.logger?.e("Cannot find $characteristicUuid to read from")
                    signalEndOfOperation()
                }
            }
            is DescriptorWrite -> with(operation) {
                connection.getDescriptor(descriptorUuid, characteristicUuid, serviceUuid)?.let { descriptor ->
                    PeripheralFramework.logger?.w("Writing to descriptor $descriptorUuid: ${payload.toHexString()}")
                    descriptor.value = payload
                    connection.writeDescriptor(descriptor)
                } ?: this@BleDispatcher.run {
                    PeripheralFramework.logger?.e("Cannot find $descriptorUuid to write to")
                    signalEndOfOperation()
                }
            }
            is DescriptorRead -> with(operation) {
                connection.getDescriptor(descriptorUuid, characteristicUuid, serviceUuid)?.let { descriptor ->
                    PeripheralFramework.logger?.w("Reading from descriptor $descriptorUuid")
                    connection.readDescriptor(descriptor)
                } ?: this@BleDispatcher.run {
                    PeripheralFramework.logger?.e("Cannot find $descriptorUuid to read from")
                    signalEndOfOperation()
                }
            }
            is EnableNotifications -> with(operation) {
                connection.getCharacteristic(characteristicUuid, serviceUuid)?.let { characteristic ->
                    val cccdUuid = UUID.fromString(CCC_DESCRIPTOR_UUID)
                    val payload = when {
                        characteristic.isIndicatable() ->
                            BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                        characteristic.isNotifiable() ->
                            BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        else ->
                            error("${characteristic.uuid} doesn't support notifications/indications")
                    }

                    characteristic.getDescriptor(cccdUuid)?.let { cccDescriptor ->
                        if (!connection.setCharacteristicNotification(characteristic, true)) {
                            PeripheralFramework.logger?.e("setCharacteristicNotification failed for ${characteristic.uuid}")
                            signalEndOfOperation()
                            return
                        }

                        PeripheralFramework.logger?.w("Enabling notifications/indications on ${characteristic.uuid}")
                        cccDescriptor.value = payload
                        connection.writeDescriptor(cccDescriptor)
                    } ?: this@BleDispatcher.run {
                        PeripheralFramework.logger?.e("${characteristic.uuid} doesn't contain the CCC descriptor!")
                        signalEndOfOperation()
                    }
                } ?: this@BleDispatcher.run {
                    PeripheralFramework.logger?.e("Cannot find $characteristicUuid! Failed to enable notifications.")
                    signalEndOfOperation()
                }
            }
            is DisableNotifications -> with(operation) {
                connection.getCharacteristic(characteristicUuid, serviceUuid)?.let { characteristic ->
                    val cccdUuid = UUID.fromString(CCC_DESCRIPTOR_UUID)
                    characteristic.getDescriptor(cccdUuid)?.let { cccDescriptor ->
                        if (!connection.setCharacteristicNotification(characteristic, false)) {
                            PeripheralFramework.logger?.e("setCharacteristicNotification failed for ${characteristic.uuid}")
                            signalEndOfOperation()
                            return
                        }

                        PeripheralFramework.logger?.w("Disabling notifications/indications on ${characteristic.uuid}")
                        cccDescriptor.value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
                        connection.writeDescriptor(cccDescriptor)
                    } ?: this@BleDispatcher.run {
                        PeripheralFramework.logger?.e("${characteristic.uuid} doesn't contain the CCC descriptor!")
                        signalEndOfOperation()
                    }
                } ?: this@BleDispatcher.run {
                    PeripheralFramework.logger?.e("Cannot find $characteristicUuid! Failed to disable notifications.")
                    signalEndOfOperation()
                }
            }
            is MtuRequest -> with(operation) {
                PeripheralFramework.logger?.w("Requesting an ATT MTU value of $mtu for ${device.macAddress}")
                connection.requestMtu(mtu)
            }
            else -> PeripheralFramework.logger?.e("Unsupported operation $operation")
        }
    }

    private val callback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val device = BleDevice(gatt.device)
            val deviceAddress = device.macAddress
            val connection = BleConnection(gatt)

            PeripheralFramework.logger?.w(
                """
                    onConnectionStateChange encountered for $deviceAddress (${device.name})
                    Status code: $status | New connection state: $newState
                    Android metadata: $androidDeviceDescription
                """.trimIndent()
            )

            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Successfully connected
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    PeripheralFramework.logger?.w("onConnectionStateChange: connected to $deviceAddress")
                    pendingConnectionMap.remove(device)
                    deviceConnectionMap[device] = connection
                    PeripheralFramework.logger?.w("Discovering services for $deviceAddress (${device.name})")
                    Handler(Looper.getMainLooper()).post {
                        gatt.discoverServices()
                    }
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    // Successfully disconnected (using gatt.disconnect() only, which we don't use)
                    PeripheralFramework.logger?.e("onConnectionStateChange: disconnected from $deviceAddress")
                    disconnect(device)
                }
            } else {
                // Connection attempt has failed
                PeripheralFramework.logger?.e("onConnectionStateChange: status $status encountered for $deviceAddress!")
                pendingConnectionMap.remove(device)
                if (pendingOperation is Connect) {
                    signalEndOfOperation()
                }
                // Add this BluetoothGatt to deviceGattMap for purpose of disconnect operation
                deviceConnectionMap[device] = connection
                disconnect(device)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            with(gatt) {
                val device = BleDevice(device)
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    PeripheralFramework.logger?.w("Discovered ${services.size} services for ${device.macAddress}.")
                    val connection = deviceConnectionMap[device] ?: BleConnection(this)
                    listeners.forEach { it.get()?.onConnectionSetupComplete?.invoke(connection) }
                } else {
                    PeripheralFramework.logger?.e("Service discovery failed due to status $status")
                    disconnect(device)
                }
            }

            if (pendingOperation is Connect) {
                signalEndOfOperation()
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            PeripheralFramework.logger?.w("ATT MTU changed to $mtu, success: ${status == BluetoothGatt.GATT_SUCCESS}")
            if (status == BluetoothGatt.GATT_SUCCESS) {
                PeripheralFramework.logger?.w("Request succeeded, current MTU is $mtu")
            } else {
                PeripheralFramework.logger?.e("Request failed with error code $status, current MTU is $mtu")
            }

            val device = BleDevice(gatt.device)
            listeners.forEach { it.get()?.onMtuChanged?.invoke(device, mtu, status.wrap()) }

            if (pendingOperation is MtuRequest) {
                signalEndOfOperation()
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            with(characteristic) {
                val device = BleDevice(gatt.device)
                listeners.forEach {
                    it.get()?.onCharacteristicRead?.invoke(device, this, status.wrap())
                }
                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        PeripheralFramework.logger?.i("Read characteristic $uuid | value: ${value.toHexString()}")
                    }
                    BluetoothGatt.GATT_READ_NOT_PERMITTED -> {
                        PeripheralFramework.logger?.e("Read not permitted for $uuid!")
                    }
                    else -> {
                        PeripheralFramework.logger?.e("Characteristic read failed for $uuid, error: $status")
                    }
                }
            }

            if (pendingOperation is CharacteristicRead) {
                signalEndOfOperation()
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            with(characteristic) {
                val device = BleDevice(gatt.device)
                listeners.forEach {
                    it.get()?.onCharacteristicWrite?.invoke(device, this, status.wrap())
                }
                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        PeripheralFramework.logger?.i("Wrote to characteristic $uuid | value: ${value.toHexString()}")
                    }
                    BluetoothGatt.GATT_WRITE_NOT_PERMITTED -> {
                        PeripheralFramework.logger?.e("Write not permitted for $uuid!")
                    }
                    else -> {
                        PeripheralFramework.logger?.e("Characteristic write failed for $uuid, error: $status")
                    }
                }
            }

            if (pendingOperation is CharacteristicWrite) {
                signalEndOfOperation()
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            with(characteristic) {
                val device = BleDevice(gatt.device)
                listeners.forEach { it.get()?.onCharacteristicChanged?.invoke(device, this) }
                PeripheralFramework.logger?.i("Characteristic $uuid changed | value: ${value.toHexString()}")
            }
        }

        override fun onDescriptorRead(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            with(descriptor) {
                val device = BleDevice(gatt.device)
                listeners.forEach {
                    it.get()?.onDescriptorRead?.invoke(device, this, status.wrap())
                }
                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        PeripheralFramework.logger?.i("Read descriptor $uuid | value: ${value.toHexString()}")
                    }
                    BluetoothGatt.GATT_READ_NOT_PERMITTED -> {
                        PeripheralFramework.logger?.e("Read not permitted for $uuid!")
                    }
                    else -> {
                        PeripheralFramework.logger?.e("Descriptor read failed for $uuid, error: $status")
                    }
                }
            }

            if (pendingOperation is DescriptorRead) {
                signalEndOfOperation()
            }
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            with(descriptor) {
                val device = BleDevice(gatt.device)
                if (isCccd() && status == BluetoothGatt.GATT_SUCCESS) {
                    onCccdWrite(gatt, value, characteristic)
                } else {
                    listeners.forEach {
                        it.get()?.onDescriptorWrite?.invoke(device, this, status.wrap())
                    }
                }
                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        PeripheralFramework.logger?.i("Wrote to descriptor $uuid | value: ${value.toHexString()}")
                    }
                    BluetoothGatt.GATT_WRITE_NOT_PERMITTED -> {
                        PeripheralFramework.logger?.e("Write not permitted for $uuid!")
                    }
                    else -> {
                        PeripheralFramework.logger?.e("Descriptor write failed for $uuid, error: $status")
                    }
                }
            }

            if (descriptor.isCccd() &&
                (pendingOperation is EnableNotifications || pendingOperation is DisableNotifications)
            ) {
                signalEndOfOperation()
            } else if (!descriptor.isCccd() && pendingOperation is DescriptorWrite) {
                signalEndOfOperation()
            }
        }

        private fun onCccdWrite(
            gatt: BluetoothGatt,
            value: ByteArray,
            characteristic: BluetoothGattCharacteristic
        ) {
            val charUuid = characteristic.uuid
            val notificationsEnabled =
                value.contentEquals(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) ||
                    value.contentEquals(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE)
            val notificationsDisabled =
                value.contentEquals(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)
            val device = BleDevice(gatt.device)

            when {
                notificationsEnabled -> {
                    PeripheralFramework.logger?.w("Notifications or indications ENABLED on $charUuid")

                    val notifyingCharacteristics =
                        notifyingCharacteristicsMap[device]?.toMutableSet()
                            ?: mutableSetOf()
                    notifyingCharacteristics.add(characteristic)
                    notifyingCharacteristicsMap[device] = notifyingCharacteristics

                    listeners.forEach {
                        it.get()?.onNotificationsEnabled?.invoke(
                            device,
                            characteristic
                        )
                    }
                }
                notificationsDisabled -> {
                    PeripheralFramework.logger?.w("Notifications or indications DISABLED on $charUuid")

                    val notifyingCharacteristics =
                        notifyingCharacteristicsMap[device]?.toMutableSet()
                    notifyingCharacteristics?.let {
                        it.remove(characteristic)
                        notifyingCharacteristicsMap[device] = it
                    }

                    listeners.forEach {
                        it.get()?.onNotificationsDisabled?.invoke(
                            device,
                            characteristic
                        )
                    }
                }
                else -> {
                    PeripheralFramework.logger?.e("Unexpected value ${value.toHexString()} on CCCD of $charUuid")
                }
            }
        }
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            with(intent) {
                if (action == BluetoothDevice.ACTION_BOND_STATE_CHANGED &&
                    hasExtra(BluetoothDevice.EXTRA_DEVICE)
                ) {
                    val bluetoothDevice = getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                        ?: return@with
                    val device = BleDevice(bluetoothDevice)
                    val previousBondState = getIntExtra(
                        BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE,
                        -1
                    ).toBondState()
                    val bondState = getIntExtra(
                        BluetoothDevice.EXTRA_BOND_STATE,
                        -1
                    ).toBondState()

                    listeners.forEach {
                        it.get()?.onBondStateChanged?.invoke(device, previousBondState, bondState)
                    }

                    if (pendingOperation is CreateBond && pendingOperation?.device == device) {
                        if (previousBondState == BondState.BONDING &&
                            bondState == BondState.BONDED
                        ) {
                            PeripheralFramework.logger?.w("Bonding with ${device.macAddress} (${device.name}) succeeded")
                            listeners.forEach {
                                it.get()?.onBondCreationSucceeded?.invoke(device)
                            }
                            signalEndOfOperation()
                        } else if (previousBondState == BondState.BONDING &&
                            bondState == BondState.NONE
                        ) {
                            PeripheralFramework.logger?.e("Bonding with ${device.macAddress} (${device.name}) failed!")
                            listeners.forEach {
                                it.get()?.onBondCreationFailed?.invoke(device)
                            }
                            signalEndOfOperation()
                        }
                    }

                    val bondTransition = "${previousBondState.description()} to ${bondState.description()}"
                    PeripheralFramework.logger?.w("${device.macAddress} bond state changed | $bondTransition")
                } else if (action == BluetoothAdapter.ACTION_STATE_CHANGED &&
                    hasExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE) &&
                    hasExtra(BluetoothAdapter.EXTRA_STATE)
                ) {
                    val previousState = getIntExtra(
                        BluetoothAdapter.EXTRA_PREVIOUS_STATE,
                        -1
                    ).toBluetoothState()
                    val newState = getIntExtra(
                        BluetoothAdapter.EXTRA_STATE,
                        -1
                    ).toBluetoothState()

                    listeners.forEach {
                        it.get()?.onBluetoothStateChanged?.invoke(previousState, newState)
                    }

                    if (previousState == BluetoothState.TURNING_OFF && newState == BluetoothState.OFF) {
                        PeripheralFramework.logger?.e("Bluetooth toggled off, disconnecting all devices")
                        deviceConnectionMap.map { it.key }.forEach { disconnect(it) }
                    }

                    val stateTransition = "${previousState.description()} to ${newState.description()}"
                    PeripheralFramework.logger?.w("Android Bluetooth state changed | $stateTransition")
                }
            }
        }

        private fun Int.toBondState() = BondState.fromBluetoothDeviceBondStatus(this)
        private fun Int.toBluetoothState() = BluetoothState.fromBluetoothAdapterState(this)
    }

    init {
        val intentFilter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        }
        appContext.registerReceiver(broadcastReceiver, intentFilter)
    }
}
