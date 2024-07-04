package io.element.android.features.messages.impl.timeline.components.event

import android.text.SpannableString
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import chat.schildi.lib.compose.thenIf
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextBasedContent
import io.element.android.libraries.matrix.ui.messages.LocalRoomMemberProfilesCache

@Composable
internal fun scGetTextWithResolvedMentions(
    content: TimelineItemTextBasedContent,
    collapsed: MutableState<Boolean>,
): CharSequence {
    val canCollapse = content.formattedCollapsedBody != null
    val formattedBody = if (collapsed.value && canCollapse) content.formattedCollapsedBody else content.formattedBody
    return getTextWithResolvedMentions(
        toFormat = formattedBody,
        content = content,
    )
}

@OptIn(ExperimentalFoundationApi::class)
internal fun Modifier.scCollapseClick(
    collapsed: MutableState<Boolean>,
    canCollapse: Boolean,
    onLongClick: () -> Unit,
) = thenIf(canCollapse) {
    combinedClickable(
        onClick = { collapsed.value = !collapsed.value },
        onLongClick = onLongClick
    )
}

@Composable // SC: Copy from upstream code in the non-extension file, then added formattedContent override
private fun getTextWithResolvedMentions(toFormat: CharSequence?, content: TimelineItemTextBasedContent): CharSequence {
    val userProfileCache = LocalRoomMemberProfilesCache.current
    val lastCacheUpdate by userProfileCache.lastCacheUpdate.collectAsState()
    val formattedBody = remember(toFormat, lastCacheUpdate) {
        toFormat?.let { formattedBody ->
            updateMentionSpans(formattedBody, userProfileCache)
            formattedBody
        }
    }
    return SpannableString(formattedBody ?: content.body)
}
