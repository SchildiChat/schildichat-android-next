/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.workmanager

import androidx.work.Data
import androidx.work.workDataOf
import dev.zacsweers.metro.Inject
import io.element.android.libraries.androidutils.json.JsonProvider
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.push.api.push.NotificationEventRequest
import timber.log.Timber

@Inject
class WorkerDataConverter(
    private val json: JsonProvider,
) {
    fun serialize(notificationEventRequests: List<NotificationEventRequest>): Result<Data> {
        return runCatchingExceptions { json().encodeToString(notificationEventRequests.map { it.toData() }) }
            .onFailure {
                Timber.e(it, "Failed to serialize notification requests")
            }
            .map { str ->
                workDataOf(REQUESTS_KEY to str)
            }
    }

    fun deserialize(data: Data): List<NotificationEventRequest>? {
        val rawRequestsJson = data.getString(REQUESTS_KEY) ?: return null
        return runCatchingExceptions {
            json().decodeFromString<List<SyncNotificationWorkManagerRequest.Data>>(rawRequestsJson).map { it.toRequest() }
        }.fold(
            onSuccess = {
                Timber.d("Deserialized ${it.size} requests")
                it
            },
            onFailure = {
                Timber.e(it, "Failed to deserialize notification requests")
                null
            }
        )
    }

    companion object {
        private const val REQUESTS_KEY = "requests"
    }
}

private fun NotificationEventRequest.toData(): SyncNotificationWorkManagerRequest.Data {
    return SyncNotificationWorkManagerRequest.Data(
        sessionId = sessionId.value,
        roomId = roomId.value,
        eventId = eventId.value,
        providerInfo = providerInfo,
    )
}

private fun SyncNotificationWorkManagerRequest.Data.toRequest(): NotificationEventRequest {
    return NotificationEventRequest(
        sessionId = SessionId(sessionId),
        roomId = RoomId(roomId),
        eventId = EventId(eventId),
        providerInfo = providerInfo,
    )
}
