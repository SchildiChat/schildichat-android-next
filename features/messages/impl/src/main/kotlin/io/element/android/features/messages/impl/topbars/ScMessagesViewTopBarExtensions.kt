package io.element.android.features.messages.impl.topbars

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import chat.schildi.components.preferences.AutoRenderedDropdown
import chat.schildi.lib.R
import chat.schildi.lib.preferences.ScPrefs
import chat.schildi.lib.preferences.value
import chat.schildi.lib.util.formatUnreadCount
import chat.schildi.theme.ScTheme
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.messages.impl.MessagesState
import io.element.android.features.messages.impl.UnclippedIconButton
import io.element.android.features.messages.impl.moveCallButtonToOverflow
import io.element.android.features.messages.impl.pinned.banner.PinnedMessagesBannerState
import io.element.android.features.messages.impl.showMarkAsReadQuickAction
import io.element.android.features.messages.impl.takeIfIsValidEventId
import io.element.android.features.messages.impl.timeline.TimelineEvents
import io.element.android.features.roomcall.api.RoomCallState
import io.element.android.libraries.designsystem.icons.CompoundDrawables
import io.element.android.libraries.designsystem.theme.components.DropdownMenu
import io.element.android.libraries.designsystem.theme.components.DropdownMenuItem
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
internal fun ScNotEncryptedIndicator(isRoomEncrypted: Boolean?) {
    if (isRoomEncrypted == false && ScPrefs.SC_TIMELINE_LAYOUT.value()) {
        Icon(
            modifier = Modifier.size(14.dp),
            imageVector = CompoundIcons.LockOff(),
            contentDescription = null,
            tint = ElementTheme.colors.iconInfoPrimary,
        )
    }
}

@Composable
internal fun RowScope.scMessagesViewTopBarActions(
    state: MessagesState,
    callState: RoomCallState,
    onJoinCallClicked: () -> Unit,
    onViewAllPinnedMessagesClick: () -> Unit,
) {
    val markAsReadAsQuickAction = showMarkAsReadQuickAction()
    if (markAsReadAsQuickAction) {
        IconButton(onClick = { state.timelineState.eventSink(TimelineEvents.MarkAsRead) }) {
            Icon(
                imageVector = Icons.Default.RemoveRedEye,
                contentDescription = stringResource(R.string.sc_action_mark_as_read),
            )
        }
    }
    if (ScPrefs.PINNED_MESSAGE_TOOLBAR_ACTION.value()) {
        val count = (state.pinnedMessagesBannerState as? PinnedMessagesBannerState.Visible)?.pinnedMessagesCount() ?: 0
        if (count > 0) {
            UnclippedIconButton(onClick = onViewAllPinnedMessagesClick) {
                BoxWithCount(count) {
                    Icon(
                        imageVector = Icons.Default.PushPin,
                        contentDescription = stringResource(io.element.android.libraries.ui.strings.R.string.common_pinned),
                    )
                }
            }
        }
    }
    if (ScPrefs.JUMP_TO_UNREAD.value()) {
        val fullyReadEventId = state.timelineState.scReadState?.fullyReadEventId?.value?.takeIfIsValidEventId()
        // TODO maybe later something like
        //  val isFullyRead = state.timelineState.isLive && state.timelineState.timelineItems.first().isEvent(fullyReadEventId)
        if (fullyReadEventId != null) {
            IconButton(onClick = { state.timelineState.eventSink(TimelineEvents.FocusOnEvent(EventId(fullyReadEventId), forReadMarker = true)) }) {
                Icon(
                    imageVector = Icons.Default.Update, // There may be a better icon
                    contentDescription = stringResource(R.string.sc_action_jump_to_unread),
                )
            }
        }
    }
    val devQuickOptions = ScPrefs.SC_DEV_QUICK_OPTIONS.value()
    val callItemInOverflow = (ScPrefs.HIDE_CALL_TOOLBAR_ACTION.value() || moveCallButtonToOverflow()) &&
        (callState as? RoomCallState.StandBy)?.canStartCall == true
    if (devQuickOptions || callItemInOverflow) {
        var showMenu by remember { mutableStateOf(false) }
        IconButton(
            onClick = { showMenu = !showMenu }
        ) {
            Icon(
                resourceId = CompoundDrawables.ic_compound_overflow_vertical,
                contentDescription = null,
            )
        }
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            if (callItemInOverflow) {
                DropdownMenuItem(
                    onClick = {
                        showMenu = false
                        onJoinCallClicked()
                    },
                    text = { Text(stringResource(CommonStrings.a11y_start_call)) },
                )
            }
            if (devQuickOptions) {
                if (ScPrefs.SYNC_READ_RECEIPT_AND_MARKER.value() && !markAsReadAsQuickAction) {
                    DropdownMenuItem(
                        onClick = {
                            showMenu = false
                            state.timelineState.eventSink(TimelineEvents.MarkAsRead)
                        },
                        text = { Text(stringResource(id = R.string.sc_action_mark_as_read)) },
                    )
                }
                ScPrefs.devQuickTweaksTimeline.forEach {
                    it.AutoRenderedDropdown(
                        onClick = { showMenu = false }
                    )
                }
            }
        }
    } else {
        Spacer(Modifier.width(8.dp))
    }
}

@Composable // Similar to UnreadCountBox from SpacesPager
private fun BoxWithCount(count: Int, offset: Dp = 6.dp, content: @Composable () -> Unit) {
    if (count <= 0) {
        content()
        return
    }
    Box {
        content()
        Box(
            modifier = Modifier
                .offset(offset, -offset)
                .background(ScTheme.exposures.unreadBadgeOnToolbarColor, RoundedCornerShape(8.dp))
                .sizeIn(minWidth = 16.dp, minHeight = 16.dp)
                .align(Alignment.TopEnd)
        ) {
            androidx.compose.material3.Text(
                text = formatUnreadCount(count.toLong()),
                color = ScTheme.exposures.colorOnAccent,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 2.dp)
            )
        }
    }
}
