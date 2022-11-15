/*
 * FailureReason.kt
 * Android Peripheral Framework
 *
 * Copyright (C) 2022 Current Health. All rights reserved.
 */

package com.currenthealth.peripheralframework

import android.bluetooth.BluetoothGatt

enum class FailureReason {
    /**
     * At least one permission is missing or has not been granted by the end-user for the requested
     * operation.
     */
    MISSING_PERMISSION,

    /**
     * The requested operation has failed because Bluetooth is either off or unavailable.
     */
    BLUETOOTH_UNAVAILABLE,

    /**
     * The requested operation has failed because of an insufficient authentication, authorization,
     * or encryption level at the Bluetooth layer.
     */
    INSUFFICIENT_SECURITY,

    /**
     * The requested operation is not allowed in the given context, e.g., trying to read the value
     * of a characteristic that is not readable, or stopping a scan when there is no ongoing scan.
     */
    NOT_ALLOWED,

    /**
     * The requested resource is not available, e.g., trying to interact with a characteristic that
     * does not exist.
     */
    RESOURCE_UNAVAILABLE,
    /**
     * The requested operation has failed because the peripheral has disconnected.
     */
    PERIPHERAL_DISCONNECTED,

    /**
     * The requested operation has timed out.
     */
    TIMEOUT,

    /**
     * The requested operation has encountered a generic failure, e.g., failing to read from or
     * write to a characteristic, or failing to establish a connection to a peripheral.
     */
    GENERIC_FAILURE,

    /**
     * The reason for the failure is unknown.
     */
    UNKNOWN;

    companion object {
        internal fun fromFailureCode(code: Int): FailureReason {
            return when (code) {
                BluetoothGatt.GATT_WRITE_NOT_PERMITTED,
                BluetoothGatt.GATT_READ_NOT_PERMITTED,
                BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED -> NOT_ALLOWED
                BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH,
                BluetoothGatt.GATT_INVALID_OFFSET -> RESOURCE_UNAVAILABLE
                BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION,
                8, // Insufficient Authorization,
                137,
                BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION -> INSUFFICIENT_SECURITY
                BluetoothGatt.GATT_FAILURE,
                BluetoothGatt.GATT_CONNECTION_CONGESTED -> GENERIC_FAILURE

                else -> UNKNOWN
            }
        }
    }
}
