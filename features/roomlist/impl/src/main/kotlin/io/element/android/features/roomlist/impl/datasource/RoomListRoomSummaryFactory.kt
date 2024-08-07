/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.roomlist.impl.datasource

import io.element.android.features.roomlist.impl.model.RoomListRoomSummary
import io.element.android.features.roomlist.impl.model.RoomSummaryDisplayType
import io.element.android.libraries.core.extensions.orEmpty
import io.element.android.libraries.dateformatter.api.LastMessageTimestampFormatter
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.eventformatter.api.RoomLastMessageFormatter
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import io.element.android.libraries.matrix.api.roomlist.RoomSummaryDetails
import io.element.android.libraries.matrix.ui.model.getAvatarData
import io.element.android.libraries.matrix.ui.model.toInviteSender
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject

class RoomListRoomSummaryFactory @Inject constructor(
    private val lastMessageTimestampFormatter: LastMessageTimestampFormatter,
    private val roomLastMessageFormatter: RoomLastMessageFormatter,
) {
    companion object {
        fun createPlaceholder(id: String): RoomListRoomSummary {
            return RoomListRoomSummary(
                id = id,
                roomId = RoomId(id),
                displayType = RoomSummaryDisplayType.PLACEHOLDER,
                name = "Short name",
                timestamp = "hh:mm",
                lastMessage = "Last message for placeholder",
                avatarData = AvatarData(id, "S", size = AvatarSize.RoomListItem),
                numberOfUnreadMessages = 0,
                numberOfUnreadMentions = 0,
                numberOfUnreadNotifications = 0,
                isMarkedUnread = false,
                userDefinedNotificationMode = null,
                hasRoomCall = false,
                isDirect = false,
                isFavorite = false,
                inviteSender = null,
                isDm = false,
                canonicalAlias = null,
                heroes = persistentListOf(),
            )
        }
    }

    fun create(roomSummary: RoomSummary.Filled): RoomListRoomSummary {
        return create(roomSummary.details)
    }

    private fun create(details: RoomSummaryDetails): RoomListRoomSummary {
        val avatarData = details.getAvatarData(size = AvatarSize.RoomListItem)
        return RoomListRoomSummary(
            id = details.roomId.value,
            roomId = details.roomId,
            name = details.name,
            numberOfUnreadMessages = details.numUnreadMessages,
            numberOfUnreadMentions = details.numUnreadMentions,
            numberOfUnreadNotifications = details.numUnreadNotifications,
            // SC start
            spaceChildren = details.spaceChildren,
            notificationCount = details.unreadNotificationCount,
            highlightCount = details.highlightCount,
            unreadCount = details.unreadCount,
            lastMessageTimestamp = details.lastMessageTimestamp,
            isLowPriority = details.isLowPriority,
            // SC end
            isMarkedUnread = details.isMarkedUnread,
            timestamp = lastMessageTimestampFormatter.format(details.lastMessageTimestamp),
            lastMessage = details.lastMessage?.let { message ->
                roomLastMessageFormatter.format(message.event, details.isDirect)
            }.orEmpty(),
            avatarData = avatarData,
            userDefinedNotificationMode = details.userDefinedNotificationMode,
            hasRoomCall = details.hasRoomCall,
            isDirect = details.isDirect,
            isFavorite = details.isFavorite,
            inviteSender = details.inviter?.toInviteSender(),
            isDm = details.isDm,
            canonicalAlias = details.canonicalAlias,
            displayType = if (details.currentUserMembership == CurrentUserMembership.INVITED) {
                RoomSummaryDisplayType.INVITE
            } else {
                RoomSummaryDisplayType.ROOM
            },
            heroes = details.heroes.map { user ->
                user.getAvatarData(size = AvatarSize.RoomListItem)
            }.toImmutableList(),
        )
    }
}
