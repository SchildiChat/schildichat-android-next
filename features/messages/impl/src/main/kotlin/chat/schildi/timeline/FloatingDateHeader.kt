package chat.schildi.timeline

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(FlowPreview::class)
@Composable
fun BoxScope.FloatingDateHeader(
    listState: LazyListState,
    timelineItems: ImmutableList<TimelineItem>,
) {
    var renderedDate by remember { mutableStateOf<String?>(null) }
    var isScrolling by remember { mutableStateOf(false) }

    // Collect date to render
    LaunchedEffect(listState, timelineItems) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastIndex }.distinctUntilChanged().collect { _ ->
            renderedDate = listState.layoutInfo.visibleItemsInfo.asReversed().firstNotNullOfOrNull { info ->
                if (info.index >= 0 && info.index < timelineItems.size) {
                    val item = timelineItems[info.index]
                    when (item) {
                        is TimelineItem.Event -> item.sentDate
                        is TimelineItem.GroupedEvents -> item.events.firstOrNull()?.sentDate
                        else -> null
                    }
                } else {
                    null
                }
            }
        }
    }

    // Collect whether user is scrolling
    LaunchedEffect(listState) {
        // Debounce: start scroll should trigger date immediately, but end scroll should delay a bit before hiding date again
        snapshotFlow { listState.isScrollInProgress }.distinctUntilChanged().debounce { if (it) 0 else 1000 }.collect {
            isScrolling = it
        }
    }

    // Render date header
    AnimatedVisibility(
        visible = renderedDate != null && isScrolling && listState.canScrollForward, // "forward" = up / towards past (if false, then reached top / no need for date header!)
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = 8.dp)
    ) {
        Text(
            renderedDate ?: "",
            Modifier
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.9f), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp),
            // Compare TimelineItemDaySeparatorView for style
            style = ElementTheme.typography.fontBodyMdMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
