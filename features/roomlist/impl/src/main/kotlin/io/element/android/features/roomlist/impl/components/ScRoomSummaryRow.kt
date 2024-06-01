/*
 * Copyright (c) 2023 New Vector Ltd
 * Copyright (c) 2023 SchildiChat
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

package io.element.android.features.roomlist.impl.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import chat.schildi.lib.preferences.ScPrefs
import chat.schildi.lib.preferences.value
import chat.schildi.lib.util.formatUnreadCount
import chat.schildi.theme.ScTheme
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.roomlist.impl.RoomListEvents
import io.element.android.features.roomlist.impl.model.RoomListRoomSummary
import io.element.android.features.roomlist.impl.model.RoomListRoomSummaryProvider
import io.element.android.features.roomlist.impl.model.RoomSummaryDisplayType
import io.element.android.libraries.core.extensions.orEmpty
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.roomListRoomMessage
import io.element.android.libraries.designsystem.theme.roomListRoomMessageDate
import io.element.android.libraries.designsystem.theme.roomListRoomName
import io.element.android.libraries.designsystem.theme.unreadIndicator
import io.element.android.libraries.ui.strings.CommonStrings
import timber.log.Timber

internal val scRowMinHeight = 84.dp

internal fun scRoomSummaryRowSupports(room: RoomListRoomSummary) = room.displayType == RoomSummaryDisplayType.ROOM

@Composable
internal fun ScRoomSummaryRow(
    room: RoomListRoomSummary,
    onClick: (RoomListRoomSummary) -> Unit,
    eventSink: (RoomListEvents) -> Unit,
    modifier: Modifier = Modifier,
    isLastIndex: Boolean,
) {
    when (room.displayType) {
        RoomSummaryDisplayType.ROOM -> {
            ScRoomSummaryRealRow(
                room = room,
                onClick = onClick,
                onLongClick = {
                    eventSink(RoomListEvents.ShowContextMenu(room))
                },
                modifier = modifier
            )
        }
        else -> {
            // Fall back to upstream for placeholder and invites (for now?)
            RoomSummaryRow(
                room = room,
                onClick = onClick,
                eventSink = eventSink,
            )
        }
    }
    if (!isLastIndex) {
        HorizontalDivider()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ScRoomSummaryRealRow(
    room: RoomListRoomSummary,
    onClick: (RoomListRoomSummary) -> Unit,
    onLongClick: (RoomListRoomSummary) -> Unit,
    modifier: Modifier = Modifier,
) {
    val clickModifier = Modifier.combinedClickable(
        onClick = { onClick(room) },
        onLongClick = { onLongClick(room) },
        indication = rememberRipple(),
        interactionSource = remember { MutableInteractionSource() }
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = scRowMinHeight)
            .then(clickModifier)
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 11.dp)
            .height(IntrinsicSize.Min),
    ) {
        Avatar(
            room
                .avatarData,
            modifier = Modifier
                .align(Alignment.CenterVertically)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                ScNameAndTimestampRow(room = room)
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                ScLastMessageAndIndicatorRow(room = room)
            }
        }
    }
}

@Composable
private fun RowScope.ScNameAndTimestampRow(room: RoomListRoomSummary) {
    // Name
    Text(
        modifier = Modifier
            .weight(1f)
            .padding(end = 16.dp),
        style = ElementTheme.typography.fontBodyLgMedium,
        text = room.name ?: stringResource(id = CommonStrings.common_no_room_name),
        color = MaterialTheme.roomListRoomName(),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        // Favorite
        if (room.isFavorite && ScPrefs.PIN_FAVORITES.value()) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.roomListRoomMessageDate(),
            )
        }
        // Low prio
        if (room.isLowPriority && ScPrefs.BURY_LOW_PRIORITY.value()) {
            Icon(
                imageVector = Icons.Default.Archive,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.roomListRoomMessageDate(),
            )
        }
        // Timestamp
        if (!room.timestamp.isNullOrEmpty()) {
            Text(
                text = room.timestamp,
                style = ElementTheme.typography.fontBodySmMedium,
                color = MaterialTheme.roomListRoomMessageDate(),
            )
        }
    }
}

@Composable
private fun RowScope.ScLastMessageAndIndicatorRow(room: RoomListRoomSummary) {
    // Last Message
    val attributedLastMessage = room.lastMessage as? AnnotatedString
        ?: AnnotatedString(room.lastMessage.orEmpty().toString())
    Text(
        modifier = Modifier
            .weight(1f)
            .padding(end = 16.dp),
        text = attributedLastMessage,
        color = MaterialTheme.roomListRoomMessage(),
        style = ElementTheme.typography.fontBodyMdRegular,
        minLines = 2,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )

    // Unread
    Row(
        modifier = Modifier.heightIn(min = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Video call
        if (room.hasRoomCall) {
            Icon(
                modifier = Modifier.size(16.dp),
                imageVector = CompoundIcons.VideoCallSolid(),
                contentDescription = null,
                tint = ElementTheme.colors.unreadIndicator,
            )
        }
        ScUnreadCounter(room)
    }
}

@Composable
private fun ScUnreadCounter(room: RoomListRoomSummary) {
    val highlightCount: Int
    val notificationCount: Int
    val unreadCount: Int
    if (ScPrefs.CLIENT_GENERATED_UNREAD_COUNTS.value()) {
        highlightCount = room.numberOfUnreadMentions
        notificationCount = room.numberOfUnreadNotifications
        unreadCount = room.numberOfUnreadMessages
    } else {
        highlightCount = room.highlightCount
        notificationCount = room.notificationCount
        unreadCount = room.unreadCount
    }
    val count: String
    val badgeColor: Color
    var outlinedBadge = false
    when {
        ScPrefs.DUAL_MENTION_UNREAD_COUNTS.value() && highlightCount > 0 && (notificationCount > highlightCount || (notificationCount == 0 && unreadCount > highlightCount)) -> {
            val fullUnreadToUse = if (notificationCount == 0) unreadCount else notificationCount
            count = "${formatUnreadCount(highlightCount)}/${formatUnreadCount(fullUnreadToUse)}"
            badgeColor = ElementTheme.colors.bgCriticalPrimary
        }
        notificationCount > 0 -> {
            count = formatUnreadCount(notificationCount)
            badgeColor = if (highlightCount > 0) ElementTheme.colors.bgCriticalPrimary else ElementTheme.colors.unreadIndicator
        }
        highlightCount > 0 -> {
            count = formatUnreadCount(highlightCount)
            badgeColor = ElementTheme.colors.bgCriticalPrimary
        }
        room.isMarkedUnread -> {
            count = "!"
            badgeColor = ElementTheme.colors.unreadIndicator
            outlinedBadge = true
        }
        unreadCount > 0 -> {
            count = formatUnreadCount(unreadCount)
            badgeColor = ScTheme.exposures.unreadBadgeColor
        }
        else -> {
            // No badge to show
            return
        }
    }
    Box (
        modifier = Modifier
            .let {
                if (outlinedBadge)
                    it.border(2.dp, badgeColor, RoundedCornerShape(30.dp))
                else
                    it.background(badgeColor, RoundedCornerShape(30.dp))
            }
            .sizeIn(minWidth = 24.dp, minHeight = 24.dp)
    ) {
        Text(
            text = count,
            color = if (outlinedBadge) badgeColor else ScTheme.exposures.colorOnAccent,
            style = MaterialTheme.typography.bodySmall.let { if (outlinedBadge) it.copy(fontWeight = FontWeight.Bold) else it },
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 4.dp)
        )
    }
}

@PreviewsDayNight
@Composable
internal fun ScRoomSummaryRowPreview(@PreviewParameter(RoomListRoomSummaryProvider::class) data: RoomListRoomSummary) = ElementPreview {
    ScRoomSummaryRow(
        room = data,
        onClick = {},
        eventSink = {},
        isLastIndex = false,
    )
}
