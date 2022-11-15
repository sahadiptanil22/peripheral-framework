package com.currenthealth.peripheralframework

import android.content.Context
import com.currenthealth.peripheralframework.internal.bluetooth.ConnectionEventListener
import com.currenthealth.peripheralframework.internal.bluetooth.DeviceAlreadyConnectedException
import com.currenthealth.peripheralframework.internal.bluetooth.Dispatcher
import com.currenthealth.peripheralframework.internal.extensions.hasRequiredBluetoothPermissions
import java.lang.ref.WeakReference

internal class BluetoothManagerImpl(context: Context) : BluetoothManager {
    private val dispatcher: Dispatcher = Dispatcher.getInstance(
        context
    )

    override var delegate: WeakReference<BluetoothManagerDelegate>? = null

    override val bluetoothStatus: BluetoothStatus
        get() {
            return when {
                dispatcher.isBluetoothEnabled() -> BluetoothStatus.ON
                !dispatcher.isBluetoothSupported() -> BluetoothStatus.UNSUPPORTED
                !PeripheralFramework.applicationContext.hasRequiredBluetoothPermissions() -> BluetoothStatus.UNAUTHORIZED
                else -> BluetoothStatus.OFF
            }
        }

    override fun connect(peripheral: BluetoothPeripheral) {
        val internalPeripheral = peripheral as? BluetoothPeripheralImpl ?: run {
            delegate?.get()?.onConnectionSetupComplete(
                peripheral,
                Outcome.Failure(FailureReason.UNKNOWN)
            )
            return
        }
        try {
            dispatcher.connect(internalPeripheral.device)
        } catch (e: DeviceAlreadyConnectedException) {
            delegate?.get()?.onConnectionSetupComplete(
                peripheral,
                Outcome.Success
            )
        }
    }

    override fun disconnect(peripheral: BluetoothPeripheral) {
        val internalPeripheral = peripheral as? BluetoothPeripheralImpl ?: run {
            delegate?.get()?.onConnectionSetupComplete(
                peripheral,
                Outcome.Failure(FailureReason.UNKNOWN)
            )
            return
        }
        try {
            dispatcher.cancelConnectionAttempt(internalPeripheral.device)
        } catch (e: DeviceAlreadyConnectedException) {
            dispatcher.disconnect(internalPeripheral.device)
        }
    }

    override fun getPeripheral(macAddress: String): BluetoothPeripheral? {
        dispatcher.getRemoteDevice(macAddress)?.let { device ->
            return BluetoothPeripheralImpl(device)
        }
        return null
    }

    private val eventListener = ConnectionEventListener(
        "com.currenthealth.peripheralframework.BluetoothManagerImpl"
    ).apply {
        dispatcher.registerListener(this)
        onConnectionSetupComplete = { connection ->
            delegate?.get()?.onConnectionSetupComplete(
                BluetoothPeripheralImpl(connection.device),
                Outcome.Success
            )
        }

        onDisconnect = { device ->
            delegate?.get()?.onDisconnect(
                BluetoothPeripheralImpl(device),
                Outcome.Success
            )
        }
    }
}
