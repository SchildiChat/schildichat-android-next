package io.element.android.features.messages.impl

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import chat.schildi.lib.preferences.LocalScPreferencesStore
import chat.schildi.lib.preferences.ScPrefs
import chat.schildi.lib.preferences.safeLookup
import chat.schildi.lib.preferences.value
import io.element.android.features.messages.impl.timeline.ScReadState
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.core.MatrixPatterns

@Composable
internal fun showMarkAsReadQuickAction(): Boolean = LocalScPreferencesStore.current.combinedSettingFlow { lookup ->
    ScPrefs.SC_DEV_QUICK_OPTIONS.safeLookup(lookup) &&
        ScPrefs.READ_MARKER_DEBUG.safeLookup(lookup) &&
        ScPrefs.SYNC_READ_RECEIPT_AND_MARKER.safeLookup(lookup) &&
        ScPrefs.MARK_READ_REQUIRES_SEEN_UNREAD_LINE.safeLookup(lookup)
}.collectAsState(false).value

@Composable
internal fun moveCallButtonToOverflow(): Boolean = showMarkAsReadQuickAction()


@Composable fun ScReadMarkerDebug(scReadState: ScReadState?) {
    if (ScPrefs.READ_MARKER_DEBUG.value()) {
        Column(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth()) {
                Text(
                    text = "LR=${scReadState?.lastReadMarkerId?.value}/${scReadState?.lastReadMarkerIndex?.intValue}",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                )
                Text(
                    text = "SR=${scReadState?.sawUnreadLine?.value}",
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
                Text(
                    text = "TS=${scReadState?.readMarkerToSet?.value}",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                )
            }
            Text(
                text = "FR=${scReadState?.fullyReadEventId?.value}",
                maxLines = 1,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .align(Alignment.CenterHorizontally),
            )
        }
    }
}

@Composable // Based on material3/IconButton, some unneeded code removed and most specifically, `clip()` removed from modifier
fun UnclippedIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable BoxScope.() -> Unit
) {
    @Suppress("DEPRECATION_ERROR")
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(40.dp) // IconButtonTokens.StateLayerSize
            //.clip(IconButtonTokens.StateLayerShape.value)
            //.background(color = colors.containerColor(enabled))
            .clickable(
                onClick = onClick,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = androidx.compose.material.ripple.rememberRipple(
                    bounded = false,
                    //radius = IconButtonTokens.StateLayerSize / 2
                    radius = 20.dp
                )
            ),
        contentAlignment = Alignment.Center,
        content = content,
    )
}

fun String.takeIfIsValidEventId() = takeIf { isNotEmpty() && MatrixPatterns.isEventId(this) }
