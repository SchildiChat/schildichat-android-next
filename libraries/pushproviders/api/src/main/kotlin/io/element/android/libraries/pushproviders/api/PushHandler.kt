/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.api

interface PushHandler {
    suspend fun handle(
        pushData: PushData,
        providerInfo: String,
    ): Boolean

    suspend fun handleInvalid(
        providerInfo: String,
        data: String,
    )

    suspend fun scHandleReceived()
    suspend fun scHandleDeferred(providerInfo: String, pushData: PushData?)
    suspend fun scHandleLookupFailure(providerInfo: String, pushData: PushData)
}
