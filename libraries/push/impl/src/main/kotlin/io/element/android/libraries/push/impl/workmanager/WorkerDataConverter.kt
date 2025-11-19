/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.workmanager

import androidx.work.Data
import androidx.work.workDataOf
import dev.zacsweers.metro.Inject
import io.element.android.libraries.androidutils.json.JsonProvider
import io.element.android.libraries.core.extensions.mapCatchingExceptions
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
    fun serialize(notificationEventRequests: List<NotificationEventRequest>): List<Result<Data>> {
        // First try to serialize all requests at once. In the vast majority of cases this will work.
        return serializeRequests(notificationEventRequests)
            .fold(
                onSuccess = {
                    listOf(Result.success(it))
                },
                onFailure = {
                    // Perform serialization on sublists, workDataOf may have failed because of size limit
                    Timber.w(it, "Failed to serialize ${notificationEventRequests.size} notification requests, trying with chunks of $CHUNK_SIZE.")
                    notificationEventRequests.chunked(CHUNK_SIZE).map { chunk ->
                        serializeRequests(chunk)
                    }
                },
            )
    }

    private fun serializeRequests(notificationEventRequests: List<NotificationEventRequest>): Result<Data> {
        return runCatchingExceptions { json().encodeToString(notificationEventRequests.map { it.toData() }) }
            .onFailure {
                Timber.e(it, "Failed to serialize notification requests")
            }
            .mapCatchingExceptions { str ->
                // Note: workDataOf can fail if the data is too large
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
        internal const val CHUNK_SIZE = 20
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
