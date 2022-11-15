package com.currenthealth.peripheralframework

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import android.util.SparseArray
import androidx.core.util.containsKey
import com.currenthealth.peripheralframework.internal.bluetooth.scan.ScanEventListener
import com.currenthealth.peripheralframework.internal.bluetooth.scan.ScanManagerInternal
import com.currenthealth.peripheralframework.internal.bluetooth.scan.ScanResultInternal
import java.lang.ref.WeakReference
import java.util.Date

internal class BleScanManager(context: Context) : ScanManager {
    private val scanListener = ScanEventListener().apply {
        onScanResult = { scanResult ->
            handleScanResult(scanResult)
        }

        onScanFailed = { errorCode -> // TODO: Parse Error Code into Error
            delegate?.get()?.onScanStarted(null, Outcome.Failure(FailureReason.UNKNOWN))
        }
    }
    private val internalScanManager: ScanManagerInternal = ScanManagerInternal.getInstance(
        context
    ).apply {
        registerListener(scanListener)
    }
    private var currentScanCriteria: ScanCriteria? = null

    override var delegate: WeakReference<ScanManagerDelegate>? = null

    override val isScanning: Boolean
        get() = internalScanManager.isScanning

    override fun startScan(criteria: ScanCriteria?) {
        val scanFilterBuilder = ScanFilter.Builder()

        criteria?.apply {
            if (macAddress != null && BluetoothAdapter.checkBluetoothAddress(macAddress)) {
                scanFilterBuilder.setDeviceAddress(macAddress)
            }
            if (deviceName != null) scanFilterBuilder.setDeviceName(deviceName)
            // Add to filter if only one entry. Otherwise leave empty and parse in scan result handling
            if (serviceUuids?.size == 1) {
                scanFilterBuilder.setServiceUuid(ParcelUuid(serviceUuids.first()))
            }
            // Add to filter if only one entry. Otherwise leave empty and parse in scan result handling
            if (manufacturerSpecificData?.size == 1) {
                val key = manufacturerSpecificData.keys.first()
                scanFilterBuilder.setManufacturerData(key, manufacturerSpecificData[key])
            }
            // Add to filter if only one entry. Otherwise leave empty and parse in scan result handling
            if (serviceData?.size == 1) {
                val key = serviceData.keys.first()
                scanFilterBuilder.setServiceData(ParcelUuid(key), serviceData[key])
            }
        }

        internalScanManager.startScan(
            filter = listOf(scanFilterBuilder.build()),
            settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build(),
            minRssi = criteria?.rssiThreshold
        )
        currentScanCriteria = criteria

        delegate?.get()?.onScanStarted(
            criteria,
            Outcome.Success
        )
    }

    override fun stopScan() {
        internalScanManager.stopScan()
        delegate?.get()?.onScanStopped(currentScanCriteria, Outcome.Success)
        currentScanCriteria = null
    }

    private fun handleScanResult(result: ScanResultInternal) {
        // Check if Scan Result contains all serviceUuids from ScanCriteria
        currentScanCriteria?.serviceUuids?.let { serviceUuids ->
            for (serviceUuid in serviceUuids) {
                val resultServiceUuids = result.scanRecord?.serviceUuids ?: return
                if (!resultServiceUuids.contains(ParcelUuid(serviceUuid))) return
            }
        }
        // Check if ScanResult contains all serviceData from ScanCriteria
        currentScanCriteria?.serviceData?.let { serviceData ->
            val resultServiceData = result.scanRecord?.serviceData ?: return
            for (key in serviceData.keys) {
                if (!resultServiceData.containsKey(ParcelUuid(key))) return
                if (!resultServiceData[ParcelUuid(key)].contentEquals(serviceData[key])) return
            }
        }
        // Check if ScanResult contains all manufacturerSpecificData from ScanCriteria
        currentScanCriteria?.manufacturerSpecificData?.let { manufacturerSpecificData ->
            val resultManufacturerSpecificData = result.scanRecord?.manufacturerSpecificData ?: return
            for (key in manufacturerSpecificData.keys) {
                if (!resultManufacturerSpecificData.containsKey(key)) return
                if (!resultManufacturerSpecificData[key].contentEquals(manufacturerSpecificData[key])) return
            }
        }

        delegate?.get()?.onScanResult(
            ScanResultImpl(
                BluetoothPeripheralImpl(result.device),
                result.rssi,
                Date(result.timestampNanos),
                result.txPower,
                result.isConnectable,
                result.scanRecord?.serviceUuids?.map { it.uuid } ?: listOf(),
                result.scanRecord?.serviceData?.mapKeys { it.key.uuid } ?: mapOf(),
                result.scanRecord?.manufacturerSpecificData ?: SparseArray<ByteArray>()
            )
        )
    }
}
