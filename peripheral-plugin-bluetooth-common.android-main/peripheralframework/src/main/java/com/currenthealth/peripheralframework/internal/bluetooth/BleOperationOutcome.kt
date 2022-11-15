/*
 * BleOperationOutcome.kt
 * Android Peripheral Framework
 *
 * Copyright (C) 2022 Current Health. All rights reserved.
 */

package com.currenthealth.peripheralframework.internal.bluetooth

import android.bluetooth.BluetoothGatt

/** A representation of a BLE operation's outcome. */
internal sealed class BleOperationOutcome {
    /** A given BLE operation has succeeded. */
    internal object Success : BleOperationOutcome()

    /** A given BLE operation has failed with an error code of [status]. */
    internal data class Failure(val status: Int) : BleOperationOutcome()
}

/** Wrap this [Int] status surfaced by the Android SDK into a [BleOperationOutcome] object. */
internal fun Int.wrap(): BleOperationOutcome = if (this == BluetoothGatt.GATT_SUCCESS) {
    BleOperationOutcome.Success
} else {
    BleOperationOutcome.Failure(this)
}
