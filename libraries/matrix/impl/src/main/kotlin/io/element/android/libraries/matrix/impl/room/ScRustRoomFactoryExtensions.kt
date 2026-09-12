package io.element.android.libraries.matrix.impl.room

import chat.schildi.matrixsdk.ScTimelineFilterSettings
import org.matrix.rustcomponents.sdk.TimelineFilter
import uniffi.matrix_sdk_ui.TimelineEventFilter

fun TimelineEventFilter?.scTimelineFilter(scTimelineFilterSettings: ScTimelineFilterSettings): TimelineFilter {
    return TimelineFilter.ScSettings(
        showRedactions = scTimelineFilterSettings.showRedactions,
        filter = this?.takeIf { !scTimelineFilterSettings.showHiddenEvents },
    )
}
