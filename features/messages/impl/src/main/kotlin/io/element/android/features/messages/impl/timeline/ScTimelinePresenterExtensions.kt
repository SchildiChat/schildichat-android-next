package io.element.android.features.messages.impl.timeline

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.room.BaseRoom
import io.element.android.libraries.matrix.api.timeline.ReceiptType
import io.element.android.libraries.matrix.api.timeline.Timeline
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun forceSetReceipts(context: Context, appScope: CoroutineScope, room: BaseRoom, scReadState: ScReadState, isSendPublicReadReceiptsEnabled: Boolean) {
    scReadState.sawUnreadLine.value = true
    val eventId = scReadState.readMarkerToSet.value ?: return
    appScope.launch {
        val toast = Toast.makeText(context, chat.schildi.lib.R.string.sc_set_read_marker_implicit_toast_start, Toast.LENGTH_LONG)
        toast.show()
        room.forceSendSingleReadReceipt(if (isSendPublicReadReceiptsEnabled) ReceiptType.READ else ReceiptType.READ_PRIVATE, eventId)
        room.forceSendSingleReadReceipt(ReceiptType.FULLY_READ, eventId)
        room.setUnreadFlag(false)
        toast.cancel()
    }
}

// Use this to define some offset next time upstream adds individual `item()` calls in the lazy column below the actual timeline's `items()` call.
// Compare TimelineView.kt's LazyColumn to double check next time floating date offset seems wrong.
fun effectiveVisibleTimelineItemIndex(index: Int) = index.coerceAtLeast(0)

fun CoroutineScope.scOnScrollFinished(
    dispatchers: CoroutineDispatchers,
    scReadState: ScReadState,
    firstVisibleIndex: Int,
    timelineItems: ImmutableList<TimelineItem>,
) = launch(dispatchers.computation) {
    // Attention: TypingNotificationView lazy list item causes us an offset by one!
    val firstVisibleTimelineIndex = effectiveVisibleTimelineItemIndex(firstVisibleIndex)
    // Get last valid EventId seen by the user, as the first index might refer to a Virtual item
    val eventId = getLastEventIdBeforeOrAt(firstVisibleTimelineIndex, timelineItems)
    if (eventId != null && firstVisibleTimelineIndex <= scReadState.lastReadMarkerIndex.intValue && eventId != scReadState.lastReadMarkerId.value) {
        // When we haven't seen unread line, allow moving readMarkerToSet into the past,
        // we only cache it until sawUnreadLine becomes true, in case user doesn't scroll after it becomes visible
        if (scReadState.sawUnreadLine.value) {
            scReadState.lastReadMarkerIndex.intValue = firstVisibleTimelineIndex
            scReadState.lastReadMarkerId.value = eventId
        }
        scReadState.readMarkerToSet.value = eventId
    }
}

private fun getLastEventIdBeforeOrAt(index: Int, items: ImmutableList<TimelineItem>): EventId? {
    for (i in index until items.count()) {
        when (val item = items[i]) {
            is TimelineItem.Event -> item.eventId?.let { return it }
            is TimelineItem.GroupedEvents -> item.events.reversed().forEach { event ->
                event.eventId?.let {
                    return it
                }
            }
            else -> {}
        }
    }
    return null
}

internal fun Int.offsetForUnreadMarkerFocus(forUnreadMarker: Boolean) = if (forUnreadMarker) (this - 1).coerceAtLeast(0) else this

