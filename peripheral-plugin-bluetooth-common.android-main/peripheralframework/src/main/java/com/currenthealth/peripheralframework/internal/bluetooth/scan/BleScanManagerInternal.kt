/*
 * BleScanManagerInternal.kt
 * Android Peripheral Framework
 *
 * Copyright (C) 2022 Current Health. All rights reserved.
 */

package com.currenthealth.peripheralframework.internal.bluetooth.scan

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import com.currenthealth.peripheralframework.PeripheralFramework
import com.currenthealth.peripheralframework.internal.bluetooth.ScannerNotAvailableException
import com.currenthealth.peripheralframework.internal.bluetooth.logAndThrow
import java.lang.ref.WeakReference

@SuppressLint("MissingPermission") // App's role to ensure permissions are available
internal class BleScanManagerInternal(
    private val appContext: Context,
) : ScanManagerInternal {

    override var isScanning = false

    /** [ScanEventListener]s registered to get notified of events. */
    private var listeners: MutableSet<WeakReference<ScanEventListener>> = mutableSetOf()
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val bluetoothManager = appContext.getSystemService(Context.BLUETOOTH_SERVICE)
            as? BluetoothManager
        bluetoothManager?.adapter
    }
    private val scanner: BluetoothLeScanner?
        get() = bluetoothAdapter?.bluetoothLeScanner
    private var minRssi: Int? = null

    override fun registerListener(listener: ScanEventListener) {
        if (listeners.map { it.get() }.contains(listener)) {
            PeripheralFramework.logger?.e("$listener is already registered with ScanManagerInternal")
        } else {
            listeners.add(WeakReference(listener))
            listeners = listeners.filter { it.get() != null }.toMutableSet()
            PeripheralFramework.logger?.d("Added listener $listener, ${listeners.size} listeners total")
        }
    }

    override fun unregisterListener(listener: ScanEventListener) {
        // Removing elements while in a loop results in a java.util.ConcurrentModificationException
        var toRemove: WeakReference<ScanEventListener>? = null
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

    override fun startScan(filter: List<ScanFilter>?, settings: ScanSettings, minRssi: Int?) {
        scanner?.let {
            PeripheralFramework.logger?.i("Starting BLE scan")
            this.minRssi = minRssi
            it.startScan(filter, settings, scanCallback)
            isScanning = true
        } ?: ScannerNotAvailableException().logAndThrow()
    }

    override fun stopScan() {
        if (bluetoothAdapter?.isEnabled == true) {
            PeripheralFramework.logger?.i("Stopping BLE scan")
            scanner?.stopScan(scanCallback)
            isScanning = false
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            minRssi?.let { minRssi ->
                if (result.rssi < minRssi) return@onScanResult
            }
            val wrappedResult = BleScanResultInternal(result)
            listeners.forEach {
                it.get()?.onScanResult?.invoke(wrappedResult)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            PeripheralFramework.logger?.e("Unable to start scan, error: $errorCode")
            isScanning = false
            listeners.forEach {
                it.get()?.onScanFailed?.invoke(errorCode)
            }
        }
    }
}
