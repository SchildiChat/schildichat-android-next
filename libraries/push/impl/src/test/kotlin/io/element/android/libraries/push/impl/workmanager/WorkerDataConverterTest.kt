/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.workmanager

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.androidutils.json.DefaultJsonProvider
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.AN_EVENT_ID_2
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID_2
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID_2
import io.element.android.libraries.push.api.push.NotificationEventRequest
import org.junit.Test

class WorkerDataConverterTest {
    @Test
    fun `ensure identity when serializing - deserializing an empty list`() {
        testIdentity(emptyList())
    }

    @Test
    fun `ensure identity when serializing - deserializing a list`() {
        testIdentity(
            listOf(
                NotificationEventRequest(
                    sessionId = A_SESSION_ID,
                    roomId = A_ROOM_ID,
                    eventId = AN_EVENT_ID,
                    providerInfo = "info1",
                ),
                NotificationEventRequest(
                    sessionId = A_SESSION_ID_2,
                    roomId = A_ROOM_ID_2,
                    eventId = AN_EVENT_ID_2,
                    providerInfo = "info2",
                ),
            )
        )
    }

    private fun testIdentity(data: List<NotificationEventRequest>) {
        val sut = WorkerDataConverter(DefaultJsonProvider())
        val serialized = sut.serialize(data).getOrThrow()
        val result = sut.deserialize(serialized)
        assertThat(result).isEqualTo(data)
    }
}
