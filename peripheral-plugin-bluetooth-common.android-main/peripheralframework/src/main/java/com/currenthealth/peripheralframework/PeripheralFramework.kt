/*
 * PeripheralFramework.kt
 * Android Peripheral Framework
 *
 * Copyright (C) 2022 Current Health. All rights reserved.
 */

package com.currenthealth.peripheralframework

import android.content.Context
import android.os.Build

/**
 * Entry point to high-level framework functionalities that are not specific to Bluetooth.
 */
object PeripheralFramework {
    internal lateinit var applicationContext: Context
        private set

    /**
     * The [FrameworkLogger] instance to be used for all logging within the framework.
     */
    var logger: FrameworkLogger? = null

    /**
     * Initialize the framework with a [Context] object that is required for most Android API
     * calls. It is recommended to call this function as soon as the application [Context] is
     * available, which is usually upon an application's `onCreate`.
     */
    fun initialize(context: Context) {
        if (!::applicationContext.isInitialized) {
            synchronized(this) {
                applicationContext = context.applicationContext
            }
        }
    }

    /**
     * Logs metadata of the Android device that the framework is currently running on.
     */
    fun logDeviceInfo() {
        logger?.i(
            "${Build.MANUFACTURER} ${Build.MODEL} running Android ${Build.VERSION.RELEASE}"
        )
    }
}
