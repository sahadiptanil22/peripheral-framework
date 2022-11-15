/*
 * BluetoothStatus.kt
 * Android Peripheral Framework
 *
 * Copyright (C) 2022 Current Health. All rights reserved.
 */

package com.currenthealth.peripheralframework

enum class BluetoothStatus {
    /**
     * Bluetooth is enabled and ready for use
     */
    ON,

    /**
     * Bluetooth is powered off
     */
    OFF,

    /**
     * Bluetooth is not available on the mobile device
     */
    UNSUPPORTED,

    /**
     * The app does not have sufficient permission to utilize Bluetooth resources on the mobile
     * device
     */
    UNAUTHORIZED
}
