/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.roomlist

import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.MatrixSpaceChildInfo
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.api.room.message.RoomMessage
import io.element.android.libraries.matrix.api.user.MatrixUser

data class RoomSummary(
    val roomId: RoomId,
    val name: String?,
    val canonicalAlias: RoomAlias?,
    val alternativeAliases: List<RoomAlias>,
    val isDirect: Boolean,
    val avatarUrl: String?,
    val lastMessage: RoomMessage?,
    val numUnreadMessages: Int,
    val numUnreadMentions: Int,
    val numUnreadNotifications: Int,
    // SC: spaces
    val isSpace: Boolean = false,
    val spaceChildren: List<MatrixSpaceChildInfo> = emptyList(),
    // SC: server-reported variants compared to SDK-generated above
    val unreadNotificationCount: Int = 0,
    val highlightCount: Int = 0,
    val unreadCount: Int = 0,
    val isLowPriority: Boolean = false,
    // SC end
    val isMarkedUnread: Boolean,
    val inviter: RoomMember?,
    val userDefinedNotificationMode: RoomNotificationMode?,
    val hasRoomCall: Boolean,
    val isDm: Boolean,
    val isFavorite: Boolean,
    val currentUserMembership: CurrentUserMembership,
    val heroes: List<MatrixUser>,
) {
    val lastMessageTimestamp = lastMessage?.originServerTs
    val aliases: List<RoomAlias>
        get() = listOfNotNull(canonicalAlias) + alternativeAliases
}
