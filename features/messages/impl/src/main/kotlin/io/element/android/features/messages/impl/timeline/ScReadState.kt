package io.element.android.features.messages.impl.timeline

import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import io.element.android.libraries.matrix.api.core.EventId

data class ScReadState(
    val lastReadMarkerIndex: MutableIntState,
    val lastReadMarkerId: MutableState<EventId?>,
    val readMarkerToSet: MutableState<EventId?>,
    val sawUnreadLine: MutableState<Boolean>,
    // For debugging
    val fullyReadEventId: MutableState<String?>,
)
