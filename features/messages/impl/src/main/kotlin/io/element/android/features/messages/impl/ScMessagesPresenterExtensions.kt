package io.element.android.features.messages.impl

import io.element.android.libraries.matrix.api.room.RoomInfo

fun RoomInfo.scRoomDisplayNameOverride() = privateRoomName?.let {
    if (name != null) {
        "$privateRoomName ($name)"
    } else {
        it
    }
}
