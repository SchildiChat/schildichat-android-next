package io.element.android.features.messages.impl.timeline

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import chat.schildi.lib.preferences.ScPrefs
import chat.schildi.lib.preferences.value
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.timeline.ReceiptType
import io.element.android.libraries.matrix.api.timeline.Timeline
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.max

data class ScReadState(
    val lastReadMarkerIndex: MutableIntState,
    val lastReadMarkerId: MutableState<EventId?>,
    val readMarkerToSet: MutableState<EventId?>,
    val sawUnreadLine: MutableState<Boolean>,
    // For debugging
    val fullyReadEventId: MutableState<String?>,
)

fun forceSetReceipts(context: Context, appScope: CoroutineScope, room: MatrixRoom, scReadState: ScReadState, isSendPublicReadReceiptsEnabled: Boolean) {
    scReadState.sawUnreadLine.value = true
    appScope.launch {
        val toast = Toast.makeText(context, chat.schildi.lib.R.string.sc_set_read_marker_implicit_toast_start, Toast.LENGTH_LONG)
        toast.show()
        room.markAsRead(if (isSendPublicReadReceiptsEnabled) ReceiptType.READ else ReceiptType.READ_PRIVATE)
        room.markAsRead(ReceiptType.FULLY_READ)
        room.setUnreadFlag(false)
        toast.cancel()
    }
}

@Composable
fun createScReadState(timeline: Timeline): ScReadState {
    val lastReadMarkerIndex = remember { mutableIntStateOf(Int.MAX_VALUE) }
    val lastReadMarkerId = remember { mutableStateOf<EventId?>(null) }
    val readMarkerToSet = remember { mutableStateOf<EventId?>(null) }
    val sawUnreadLine = remember { mutableStateOf(false) }
    val fullyReadEventId = remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        fullyReadEventId.value = timeline.fullyReadEventId()
    }
    return ScReadState(
        lastReadMarkerIndex,
        lastReadMarkerId,
        readMarkerToSet,
        sawUnreadLine,
        fullyReadEventId,
    )
}

@Composable
fun ScReadTracker(
    appScope: CoroutineScope,
    scUnreadState: ScReadState,
    isSendPublicReadReceiptsEnabled: Boolean,
    timeline: Timeline,
    onBackPressed: () -> Unit
) {
    val clickedBack = remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (ScPrefs.SYNC_READ_RECEIPT_AND_MARKER.value()) {
        val debug = ScPrefs.READ_MARKER_DEBUG.value()
        val allowMarkAsRead = scUnreadState.sawUnreadLine.value || !ScPrefs.MARK_READ_REQUIRES_SEEN_UNREAD_LINE.value()
        BackHandler(enabled = !clickedBack.value && allowMarkAsRead && scUnreadState.readMarkerToSet.value != null) {
            scUnreadState.readMarkerToSet.value?.let { eventId ->
                appScope.launch {
                    val toast = Toast.makeText(
                        context,
                        if (debug)
                            context.getString(chat.schildi.lib.R.string.sc_set_read_marker_toast_start, eventId.value)
                        else
                            context.getString(chat.schildi.lib.R.string.sc_set_read_marker_implicit_toast_start),
                        Toast.LENGTH_LONG
                    )
                    toast.show()
                    // TODO? force only necessary when latest message sent by self and unread-count is not zero?
                    timeline.forceSendReadReceipt(eventId, if (isSendPublicReadReceiptsEnabled) ReceiptType.READ else ReceiptType.READ_PRIVATE)
                    timeline.forceSendReadReceipt(eventId, ReceiptType.FULLY_READ)
                    toast.cancel()
                    onBackPressed()
                }
            }
            clickedBack.value = true
        }
        // TODO doesn't work, timeline get's closed before send?
        /*
        OnLifecycleEvent { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE && sawUnreadLine.value) {
                timber.log.Timber.i("SC_DBG SET ${readMarkerToSet.value}")
                readMarkerToSet.value?.let { eventId ->
                    appScope.launch {
                        timeline.sendReadReceipt(eventId, if (isSendPublicReadReceiptsEnabled) ReceiptType.READ else ReceiptType.READ_PRIVATE)
                        timeline.sendReadReceipt(eventId, ReceiptType.FULLY_READ)
                        timber.log.Timber.i("SC_DBG SET DONE $eventId")
                    }
                }
            }
        }
         */
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
        val item = items[i]
        if (item is TimelineItem.Event) {
            return item.eventId
        }
    }
    return null
}

internal fun Int.offsetForUnreadMarkerFocus(forUnreadMarker: Boolean) = if (forUnreadMarker) (this - 1).coerceAtLeast(0) else this

