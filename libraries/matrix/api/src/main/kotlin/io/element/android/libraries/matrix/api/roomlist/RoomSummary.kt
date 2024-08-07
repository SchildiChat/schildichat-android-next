/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.libraries.matrix.api.roomlist

import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.MatrixSpaceChildInfo
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.api.room.message.RoomMessage
import io.element.android.libraries.matrix.api.user.MatrixUser

sealed interface RoomSummary {
    data class Empty(val identifier: String) : RoomSummary
    data class Filled(val details: RoomSummaryDetails) : RoomSummary

    fun identifier(): String {
        return when (this) {
            is Empty -> identifier
            is Filled -> details.roomId.value
        }
    }
}

data class RoomSummaryDetails(
    val roomId: RoomId,
    val name: String?,
    val canonicalAlias: RoomAlias?,
    val isDirect: Boolean,
    val avatarUrl: String?,
    val lastMessage: RoomMessage?,
    val numUnreadMessages: Int,
    val numUnreadMentions: Int,
    val numUnreadNotifications: Int,
    // SC: spaces
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
}
