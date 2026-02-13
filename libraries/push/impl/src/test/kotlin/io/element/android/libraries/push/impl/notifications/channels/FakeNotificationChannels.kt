/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications.channels

import io.element.android.libraries.matrix.api.core.SessionId

class FakeNotificationChannels(
    var channelForIncomingCall: (ring: Boolean) -> String = { _ -> "" },
    var channelIdForMessage: (sessionId: SessionId, noisy: Boolean) -> String = { _, _ -> "" },
    var channelIdForTest: () -> String = { "" }
) : NotificationChannels {
    override fun getChannelForIncomingCall(ring: Boolean): String {
        return channelForIncomingCall(ring)
    }

    override fun getChannelIdForMessage(sessionId: SessionId, noisy: Boolean): String {
        return channelIdForMessage(sessionId, noisy)
    }

    override fun getChannelIdForTest(): String {
        return channelIdForTest()
    }
}
