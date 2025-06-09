/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.timeline.ReceiptType
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

interface MarkAsFullyRead {
    operator fun invoke(roomId: RoomId, eventId: EventId?, withReadReceipt: ReceiptType?)
}

@ContributesBinding(SessionScope::class)
class DefaultMarkAsFullyRead @Inject constructor(
    private val matrixClient: MatrixClient,
) : MarkAsFullyRead {
    override fun invoke(roomId: RoomId, eventId: EventId?, withReadReceipt: ReceiptType?) {
        matrixClient.sessionCoroutineScope.launch {
            matrixClient.getRoom(roomId)?.use { room ->
                if (eventId != null) { // SC read tracking
                    room.forceSendSingleReadReceipt(receiptType = ReceiptType.FULLY_READ, eventId = eventId)
                        .onFailure { Timber.e("Failed to send fully_read for room $roomId", it) }
                    if (withReadReceipt != null) {
                        room.forceSendSingleReadReceipt(receiptType = withReadReceipt, eventId = eventId)
                            .onFailure { Timber.e("Failed to send aux read receipt for room $roomId", it) }
                    }
                    return@launch
                }
                room.markAsRead(receiptType = ReceiptType.FULLY_READ)
                    .onFailure {
                        Timber.e("Failed to mark room $roomId as fully read", it)
                    }
            }
        }
    }
}
