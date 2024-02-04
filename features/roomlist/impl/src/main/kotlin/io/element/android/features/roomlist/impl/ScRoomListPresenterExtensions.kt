package io.element.android.features.roomlist.impl

import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.timeline.ReceiptType
import timber.log.Timber

internal suspend fun RoomListPresenter.setMarkedAsRead(client: MatrixClient, event: RoomListEvents.SetMarkedAsRead) {
    val room = client.getRoom(event.roomId)
    if (room == null) {
        Timber.e("Unable to find room ${event.roomId} to set read=${event.read}")
        return
    }
    if (event.read) {
        room.markAsReadAndSendReadReceipt(ReceiptType.READ_PRIVATE)
    } else {
        room.markAsUnread()
    }
}
