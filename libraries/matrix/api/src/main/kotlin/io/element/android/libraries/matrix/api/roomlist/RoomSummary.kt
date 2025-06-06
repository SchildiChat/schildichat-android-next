/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.roomlist

import io.element.android.libraries.matrix.api.room.RoomInfo
import io.element.android.libraries.matrix.api.room.message.RoomMessage

data class RoomSummary(
    val info: RoomInfo,
    val lastMessage: RoomMessage?,
) {
    val roomId = info.id
    val lastMessageTimestamp = lastMessage?.originServerTs
    val isOneToOne get() = info.activeMembersCount == 2L
}
