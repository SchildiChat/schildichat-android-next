package io.element.android.features.messages.impl.timeline.components.event

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import chat.schildi.lib.preferences.ScPrefs
import chat.schildi.lib.preferences.value
import io.element.android.features.messages.impl.timeline.components.layout.ContentAvoidingLayoutData
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemUnknownContent
import io.element.android.libraries.designsystem.icons.CompoundDrawables
import io.element.android.libraries.matrix.api.timeline.item.event.TimelineItemDebugInfoProvider
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun ScTimelineItemUnknownView(
    @Suppress("UNUSED_PARAMETER") content: TimelineItemUnknownContent,
    debugInfoProvider: TimelineItemDebugInfoProvider?,
    onContentLayoutChange: (ContentAvoidingLayoutData) -> Unit,
    modifier: Modifier = Modifier
) {
    if (ScPrefs.VIEW_HIDDEN_EVENTS.value() && debugInfoProvider != null) {
        val eventType = remember(debugInfoProvider) {
            debugInfoProvider().originalJson
        } ?: stringResource(CommonStrings.common_unsupported_event)
        TimelineItemInformativeView(
            text = eventType,
            iconDescription = stringResource(id = CommonStrings.common_unsupported_event),
            iconResourceId = CompoundDrawables.ic_compound_info_solid,
            onContentLayoutChange = onContentLayoutChange,
            modifier = modifier
        )
    } else {
        TimelineItemUnknownView(content, onContentLayoutChange, modifier)
    }
}
