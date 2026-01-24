package io.element.android.features.messages.impl.timeline.components.event

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import chat.schildi.lib.R
import chat.schildi.lib.preferences.ScPrefs
import chat.schildi.lib.preferences.value
import chat.schildi.theme.ScTheme
import com.beeper.android.messageformat.MatrixBodyParseResult
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.location.api.Location
import io.element.android.features.messages.impl.timeline.components.layout.ContentAvoidingLayout
import io.element.android.features.messages.impl.timeline.components.layout.ContentAvoidingLayoutData
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemLocationContent
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.placeholderBackground
import io.element.android.libraries.textcomposer.ElementRichTextEditorStyle
import io.element.android.wysiwyg.compose.EditorStyledText

private val ICON_SIZE = 24.dp
private val ICON_PADDING = 4.dp
private val ICON_MARGIN = 10.dp
private val ICON_RESERVED_WIDTH = ICON_SIZE + ICON_PADDING * 2 + ICON_MARGIN + 4.dp

/**
 * Why to replace a nice-looking map image with a dumb text?
 * Because free-tier MapTiler doesn't give us static images and Element doesn't want forks to use their API keys anymore. :/
 * (I know we *could* use the Element key anyway, but let's be nice)
 */
@Composable
fun ScTimelineItemLocationView(
    content: TimelineItemLocationContent,
    onContentLayoutChange: (ContentAvoidingLayoutData) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = stringResource(R.string.event_placeholder_location),
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .size(ICON_SIZE)
                .background(MaterialTheme.colorScheme.background, CircleShape)
                .padding(ICON_PADDING)
        )
        Spacer(Modifier.width(ICON_MARGIN))
        val text = (content.description ?: content.body).takeIf { it.isNotBlank() } ?: stringResource(R.string.event_placeholder_location)
        CompositionLocalProvider(
            LocalContentColor provides ElementTheme.colors.textPrimary,
            LocalTextStyle provides if (ScPrefs.EL_TYPOGRAPHY.value()) ElementTheme.typography.fontBodyLgRegular else ElementTheme.typography.fontBodyMdRegular
        ) {
            Box(modifier.semantics { contentDescription = text }) {
                if (!ScPrefs.LEGACY_MESSAGE_RENDERING.value()) {
                    ScTimelineItemTextView(
                        content = MatrixBodyParseResult(text),
                        modifier = modifier,
                        onContentLayoutChange = onContentLayoutChange,
                    )
                    return@Box
                }
                EditorStyledText(
                    text = text,
                    style = ElementRichTextEditorStyle.textStyle(),
                    onTextLayout = ContentAvoidingLayout.measureLegacyLastTextLine(
                        onContentLayoutChange = onContentLayoutChange,
                        extraWidth = ICON_RESERVED_WIDTH
                    ),
                    releaseOnDetach = false,
                )
            }
        }
    }
}

@PreviewsDayNight
@Composable
fun ScTimelineItemLocationPreview() = ElementPreview {
    Column(Modifier.background(ScTheme.exposures.bubbleBgIncoming ?: ElementTheme.colors.placeholderBackground)) {
        ScTimelineItemLocationView(
            content = TimelineItemLocationContent(
                "Body",
                Location(0.0, 0.0, 0f),
                "Description"
            ),
            onContentLayoutChange = {},
        )
    }
}
