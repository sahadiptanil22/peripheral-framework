/*
 * CharacteristicWriteTypeInternal.kt
 * Android Peripheral Framework
 *
 * Copyright (C) 2022 Current Health. All rights reserved.
 */

package com.currenthealth.peripheralframework.internal.bluetooth

import com.currenthealth.peripheralframework.CharacteristicWriteType

/**
 * Write type to be used for a Bluetooth GATT characteristic write operation.
 */
internal enum class CharacteristicWriteTypeInternal {
    /**
     * The write type used depends on the characteristic's properties. If it supports writes with
     * response, [Dispatcher] will always send write requests. However, if the
     * characteristic only supports writes without response, [Dispatcher] will then send
     * write commands.
     */
    DEFAULT,

    /**
     * Always use writes with response (write requests).
     */
    WITH_RESPONSE,

    /**
     * Always use writes without response (write commands).
     */
    WITHOUT_RESPONSE;

    companion object {
        fun fromCharacteristicWriteType(type: CharacteristicWriteType): CharacteristicWriteTypeInternal {
            return when (type) {
                CharacteristicWriteType.AUTOMATIC -> DEFAULT
                CharacteristicWriteType.WITH_RESPONSE -> WITH_RESPONSE
                CharacteristicWriteType.WITHOUT_RESPONSE -> WITHOUT_RESPONSE
            }
        }
    }
}
