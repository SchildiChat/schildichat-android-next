/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomlist.impl.datasource

import io.element.android.features.roomlist.impl.model.RoomListRoomSummary
import io.element.android.features.roomlist.impl.model.RoomSummaryDisplayType
import io.element.android.libraries.core.extensions.orEmpty
import io.element.android.libraries.dateformatter.api.DateFormatter
import io.element.android.libraries.dateformatter.api.DateFormatterMode
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.eventformatter.api.RoomLastMessageFormatter
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.isDm
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import io.element.android.libraries.matrix.ui.model.getAvatarData
import io.element.android.libraries.matrix.ui.model.toInviteSender
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject

class RoomListRoomSummaryFactory @Inject constructor(
    private val dateFormatter: DateFormatter,
    private val roomLastMessageFormatter: RoomLastMessageFormatter,
) {
    fun create(roomSummary: RoomSummary): RoomListRoomSummary {
        val roomInfo = roomSummary.info
        val avatarData = roomInfo.getAvatarData(size = AvatarSize.RoomListItem)
        return RoomListRoomSummary(
            id = roomSummary.roomId.value,
            roomId = roomSummary.roomId,
            name = roomInfo.name,
            numberOfUnreadMessages = roomInfo.numUnreadMessages,
            numberOfUnreadMentions = roomInfo.numUnreadMentions,
            numberOfUnreadNotifications = roomInfo.numUnreadNotifications,
            // SC start
            isSpace = roomInfo.isSpace,
            canUserManageSpaces = roomInfo.canUserManageSpaces,
            spaceChildren = roomInfo.spaceChildren,
            notificationCount = roomInfo.notificationCount,
            highlightCount = roomInfo.highlightCount,
            unreadCount = roomInfo.unreadCount,
            lastMessageTimestamp = roomSummary.lastMessageTimestamp,
            isLowPriority = roomInfo.isLowPriority,
            // SC end
            isMarkedUnread = roomInfo.isMarkedUnread,
            timestamp = dateFormatter.format(
                timestamp = roomSummary.lastMessageTimestamp,
                mode = DateFormatterMode.TimeOrDate,
                useRelative = true,
            ),
            lastMessage = roomSummary.lastMessage?.let { message ->
                roomLastMessageFormatter.format(message.event, roomInfo.isDm)
            }.orEmpty(),
            avatarData = avatarData,
            userDefinedNotificationMode = roomInfo.userDefinedNotificationMode,
            hasRoomCall = roomInfo.hasRoomCall,
            isDirect = roomInfo.isDirect,
            isFavorite = roomInfo.isFavorite,
            inviteSender = roomInfo.inviter?.toInviteSender(),
            isDm = roomInfo.isDm,
            canonicalAlias = roomInfo.canonicalAlias,
            displayType = when (roomInfo.currentUserMembership) {
                CurrentUserMembership.INVITED -> {
                    RoomSummaryDisplayType.INVITE
                }
                CurrentUserMembership.KNOCKED -> {
                    RoomSummaryDisplayType.KNOCKED
                }
                else -> {
                    RoomSummaryDisplayType.ROOM
                }
            },
            heroes = roomInfo.heroes.map { user ->
                user.getAvatarData(size = AvatarSize.RoomListItem)
            }.toImmutableList(),
        )
    }
}
