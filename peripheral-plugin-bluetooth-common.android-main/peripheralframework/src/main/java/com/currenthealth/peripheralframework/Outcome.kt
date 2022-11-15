/*
 * Outcome.kt
 * Android Peripheral Framework
 *
 * Copyright (C) 2022 Current Health. All rights reserved.
 */

package com.currenthealth.peripheralframework

import com.currenthealth.peripheralframework.internal.bluetooth.BleOperationOutcome

sealed class Outcome {
    object Success : Outcome()
    data class Failure(val reason: FailureReason) : Outcome()
    object Unknown : Outcome()

    internal companion object {
        fun fromBleOperationOutcome(outcome: BleOperationOutcome): Outcome {
            return when (outcome) {
                is BleOperationOutcome.Success -> Success
                is BleOperationOutcome.Failure -> {
                    return Failure(FailureReason.fromFailureCode(outcome.status))
                }
            }
        }
    }
}
