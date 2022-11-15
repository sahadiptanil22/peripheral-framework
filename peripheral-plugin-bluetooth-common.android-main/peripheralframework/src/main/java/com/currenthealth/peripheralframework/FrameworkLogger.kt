/*
 * FrameworkLogger.kt
 * Android Peripheral Framework
 *
 * Copyright (C) 2022 Current Health. All rights reserved.
 */

package com.currenthealth.peripheralframework

import android.util.Log

/**
 * A logger interface that can be implemented and passed into the framework by setting
 * [PeripheralFramework.logger] to redirect all debug log outputs to.
 */
interface FrameworkLogger {
    /**
     * Log a debug message.
     */
    fun d(logText: String)

    /**
     * Log an informational message.
     */
    fun i(logText: String)

    /**
     * Log a warning message.
     */
    fun w(logText: String)

    /**
     * Log an error message.
     */
    fun e(logText: String)
}

/**
 * A simple implementation of [FrameworkLogger] that pipes all log outputs to Logcat.
 */
class LogcatDefaultLogger : FrameworkLogger {
    private val tag = "FrameworkLogger"

    override fun d(logText: String) {
        Log.d(tag, logText)
    }

    override fun i(logText: String) {
        Log.i(tag, logText)
    }

    override fun w(logText: String) {
        Log.w(tag, logText)
    }

    override fun e(logText: String) {
        Log.e(tag, logText)
    }
}
