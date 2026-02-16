package io.element.android.libraries.matrix.impl.room

import chat.schildi.matrixsdk.ScTimelineFilterSettings
import org.matrix.rustcomponents.sdk.TimelineEventFilter
import org.matrix.rustcomponents.sdk.TimelineFilter

fun TimelineEventFilter?.scTimelineFilter(scTimelineFilterSettings: ScTimelineFilterSettings): TimelineFilter {
    return TimelineFilter.ScSettings(
        showRedactions = scTimelineFilterSettings.showRedactions,
        filter = this?.takeIf { !scTimelineFilterSettings.showHiddenEvents },
    )
}
