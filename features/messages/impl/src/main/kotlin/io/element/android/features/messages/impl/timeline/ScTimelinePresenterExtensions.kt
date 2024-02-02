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

fun forceSetReceipts(appScope: CoroutineScope, timeline: MatrixTimeline, scReadState: ScReadState, isSendPublicReadReceiptsEnabled: Boolean) {
    scReadState.sawUnreadLine.value = true
    scReadState.readMarkerToSet.value?.let { eventId ->
        appScope.launch {
            timeline.sendReadReceipt(eventId, if (isSendPublicReadReceiptsEnabled) ReceiptType.READ else ReceiptType.READ_PRIVATE)
            timeline.sendReadReceipt(eventId, ReceiptType.FULLY_READ)
        }
    }
}

@Composable
fun createScReadState(): ScReadState {
    val lastReadMarkerIndex = remember { mutableIntStateOf(Int.MAX_VALUE) }
    val lastReadMarkerId = remember { mutableStateOf<EventId?>(null) }
    val readMarkerToSet = remember { mutableStateOf<EventId?>(null) }
    val sawUnreadLine = remember { mutableStateOf(false) }
    return ScReadState(
        lastReadMarkerIndex,
        lastReadMarkerId,
        readMarkerToSet,
        sawUnreadLine,
    )
}

@Composable
fun ScReadTracker(appScope: CoroutineScope, scUnreadState: ScReadState, isSendPublicReadReceiptsEnabled: Boolean, timeline: MatrixTimeline, onBackPressed: () -> Unit) {
    val clickedBack = remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (ScPrefs.SYNC_READ_RECEIPT_AND_MARKER.value()) {
        BackHandler(enabled = !clickedBack.value && scUnreadState.sawUnreadLine.value && scUnreadState.readMarkerToSet.value != null) {
            scUnreadState.readMarkerToSet.value?.let { eventId ->
                appScope.launch {
                    val toast = Toast.makeText(context, context.getString(chat.schildi.lib.R.string.sc_set_read_marker_toast_start, eventId.value), Toast.LENGTH_LONG)
                    toast.show()
                    timeline.sendReadReceipt(eventId, if (isSendPublicReadReceiptsEnabled) ReceiptType.READ else ReceiptType.READ_PRIVATE)
                    timeline.sendReadReceipt(eventId, ReceiptType.FULLY_READ)
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

