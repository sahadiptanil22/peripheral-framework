/*
 * CharacteristicWriteType.kt
 * Android Peripheral Framework
 *
 * Copyright (C) 2022 Current Health. All rights reserved.
 */

package com.currenthealth.peripheralframework

enum class CharacteristicWriteType {
    /**
     * Perform the characteristic write using a write request, or write with response.
     */
    WITH_RESPONSE,

    /**
     * Perform the characteristic write using a write command, or write without response.
     */
    WITHOUT_RESPONSE,

    /**
     * Perform the characteristic write using the write type inferred from the characteristic’s
     * property flags. If a characteristic supports both write types, the write will be performed
     * using a write request ([WITH_RESPONSE]).
     */
    AUTOMATIC
}
