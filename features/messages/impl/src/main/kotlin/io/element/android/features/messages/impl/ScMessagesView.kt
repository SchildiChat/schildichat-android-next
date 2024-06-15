package io.element.android.features.messages.impl

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import chat.schildi.components.preferences.AutoRenderedDropdown
import chat.schildi.lib.R
import chat.schildi.lib.preferences.ScPrefs
import chat.schildi.lib.preferences.value
import io.element.android.features.messages.impl.timeline.ScReadState
import io.element.android.features.messages.impl.timeline.TimelineEvents
import io.element.android.libraries.designsystem.icons.CompoundDrawables
import io.element.android.libraries.designsystem.theme.components.DropdownMenu
import io.element.android.libraries.designsystem.theme.components.DropdownMenuItem
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.MatrixPatterns
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
private fun showMarkAsReadQuickAction(): Boolean = ScPrefs.SC_DEV_QUICK_OPTIONS.value() && ScPrefs.READ_MARKER_DEBUG.value() && ScPrefs.SYNC_READ_RECEIPT_AND_MARKER.value()

@Composable
internal fun moveCallButtonToOverflow(): Boolean = showMarkAsReadQuickAction()

@Composable
internal fun RowScope.scMessagesViewTopBarActions(
    state: MessagesState,
    callState: RoomCallState,
    onJoinCallClicked: () -> Unit,
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
    if (ScPrefs.SC_DEV_QUICK_OPTIONS.value()) {
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
            if (moveCallButtonToOverflow() && callState != RoomCallState.DISABLED) {
                DropdownMenuItem(
                    onClick = {
                        showMenu = false
                        onJoinCallClicked()
                    },
                    text = { Text(stringResource(CommonStrings.a11y_start_call)) },
                )
            }
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
    } else {
        Spacer(Modifier.width(8.dp))
    }
}

@Composable fun ScReadMarkerDebug(scReadState: ScReadState?) {
    if (ScPrefs.READ_MARKER_DEBUG.value()) {
        Column(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth()) {
                Text(
                    text = "LR=${scReadState?.lastReadMarkerId?.value.toString()}",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                )
                Text(
                    text = "SR=${scReadState?.sawUnreadLine?.value}",
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
                Text(
                    text = "TS=${scReadState?.readMarkerToSet?.value.toString()}",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                )
            }
            scReadState?.fullyReadEventId?.value?.takeIfIsValidEventId()?.let {
                Text(
                    text = "FR=$it",
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .align(Alignment.CenterHorizontally),
                )
            }
        }
    }
}

fun String.takeIfIsValidEventId() = takeIf { isNotEmpty() && MatrixPatterns.isEventId(this) }
