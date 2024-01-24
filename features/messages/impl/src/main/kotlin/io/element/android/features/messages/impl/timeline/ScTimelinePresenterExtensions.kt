package io.element.android.features.messages.impl.timeline

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import chat.schildi.lib.preferences.ScPrefs
import chat.schildi.lib.preferences.value
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.timeline.MatrixTimeline
import io.element.android.libraries.matrix.api.timeline.ReceiptType
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

data class ScReadState(
    val lastReadMarkerIndex: MutableIntState,
    val lastReadMarkerId: MutableState<EventId?>,
    val readMarkerToSet: MutableState<EventId?>,
    val sawUnreadLine: MutableState<Boolean>,
)
@Composable
fun ScReadTracker(appScope: CoroutineScope, timeline: MatrixTimeline, onBackPressed: () -> Unit): ScReadState {
    val lastReadMarkerIndex = rememberSaveable { mutableIntStateOf(Int.MAX_VALUE) }
    val lastReadMarkerId = rememberSaveable { mutableStateOf<EventId?>(null) }
    val readMarkerToSet = rememberSaveable { mutableStateOf<EventId?>(null) }
    val sawUnreadLine = rememberSaveable { mutableStateOf(false) }

    val clickedBack = remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (ScPrefs.SYNC_READ_RECEIPT_AND_MARKER.value()) {
        BackHandler(enabled = !clickedBack.value && sawUnreadLine.value && readMarkerToSet.value != null) {
            readMarkerToSet.value?.let { eventId ->
                appScope.launch {
                    val toast = Toast.makeText(context, context.getString(chat.schildi.lib.R.string.sc_set_read_marker_toast_start, eventId.value), Toast.LENGTH_LONG)
                    toast.show()
                    timeline.sendReadReceipt(eventId, ReceiptType.READ)
                    timeline.sendReadReceipt(eventId, ReceiptType.FULLY_READ)
                    toast.cancel()
                    Toast.makeText(context, context.getString(chat.schildi.lib.R.string.sc_set_read_marker_toast_end, eventId.value), Toast.LENGTH_SHORT).show()
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
                        timeline.sendReadReceipt(eventId, ReceiptType.READ)
                        timeline.sendReadReceipt(eventId, ReceiptType.FULLY_READ)
                        timber.log.Timber.i("SC_DBG SET DONE $eventId")
                    }
                }
            }
        }
         */
    }

    return ScReadState(
        lastReadMarkerIndex,
        lastReadMarkerId,
        readMarkerToSet,
        sawUnreadLine,
    )
}

fun CoroutineScope.scOnScrollFinished(
    dispatchers: CoroutineDispatchers,
    scReadState: ScReadState,
    firstVisibleIndex: Int,
    timelineItems: ImmutableList<TimelineItem>,
) = launch(dispatchers.computation) {
    // Get last valid EventId seen by the user, as the first index might refer to a Virtual item
    val eventId = getLastEventIdBeforeOrAt(firstVisibleIndex, timelineItems)
    if (eventId != null && firstVisibleIndex <= scReadState.lastReadMarkerIndex.intValue && eventId != scReadState.lastReadMarkerId.value) {
        // When we haven't seen unread line, allow moving readMarkerToSet into the past,
        // we only cache it until sawUnreadLine becomes true, in case user doesn't scroll after it becomes visible
        if (scReadState.sawUnreadLine.value) {
            scReadState.lastReadMarkerIndex.intValue = firstVisibleIndex
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

