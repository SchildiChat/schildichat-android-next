/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.pushgateway

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PushGatewayNotifyBody(
    /**
     * Required. Information about the push notification
     */
    @SerialName("notification")
    val notification: PushGatewayNotification
)
