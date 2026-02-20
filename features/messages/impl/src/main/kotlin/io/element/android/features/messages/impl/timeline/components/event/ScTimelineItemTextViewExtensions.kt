package io.element.android.features.messages.impl.timeline.components.event

import android.text.SpannableStringBuilder
import android.text.Spanned
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.core.text.getSpans
import chat.schildi.lib.preferences.ScPrefs
import chat.schildi.lib.preferences.value
import chat.schildi.matrixsdk.containsOnlyEmojis
import chat.schildi.theme.scBubbleFont
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import com.beeper.android.messageformat.InlineImageInfo
import com.beeper.android.messageformat.MatrixBodyParseResult
import com.beeper.android.messageformat.MatrixStyledFormattedText
import com.beeper.android.messageformat.toInlineContent
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.messages.impl.timeline.components.layout.ContentAvoidingLayoutData
import io.element.android.features.messages.impl.timeline.factories.event.LocalMatrixBodyDrawStyle
import io.element.android.features.messages.impl.timeline.factories.event.LocalMatrixBodyFormatter
import io.element.android.features.messages.impl.timeline.factories.event.matrixBodyDrawStyle
import io.element.android.features.messages.impl.timeline.factories.event.matrixBodyFormatter
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContentWithAttachment
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextBasedContent
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.ui.media.MediaRequestData
import io.element.android.wysiwyg.link.Link
import io.element.android.wysiwyg.view.spans.InlineImageSpan
import kotlin.math.roundToInt

// All values in DP here
private const val MAX_IMAGE_WIDTH = 128
private const val MAX_IMAGE_HEIGHT = 128
private const val MIN_IMAGE_WIDTH = 8
private const val MIN_IMAGE_HEIGHT = 8

@Composable
fun ScTimelineItemTextView(
    content: TimelineItemTextBasedContent,
    onLinkLongClick: (Link) -> Unit,
    modifier: Modifier = Modifier,
    onContentLayoutChange: (ContentAvoidingLayoutData) -> Unit = {},
) {
    ScTimelineItemTextView(
        content = content.formattedBodySc,
        onLinkLongClick = onLinkLongClick,
        modifier = modifier,
        onContentLayoutChange = onContentLayoutChange,
    )
}

@Composable
fun ScTimelineItemTextView(
    content: TimelineItemEventContentWithAttachment,
    onLinkLongClick: (Link) -> Unit,
    modifier: Modifier = Modifier,
    onContentLayoutChange: (ContentAvoidingLayoutData) -> Unit = {},
) {
    ScTimelineItemTextView(
        content = content.formattedCaptionSc ?: MatrixBodyParseResult(content.caption ?: ""),
        onLinkLongClick = onLinkLongClick,
        modifier = modifier,
        onContentLayoutChange = onContentLayoutChange,
    )
}

@Composable
fun ScTimelineItemTextView(
    content: MatrixBodyParseResult,
    onLinkLongClick: (Link) -> Unit,
    modifier: Modifier = Modifier,
    onContentLayoutChange: (ContentAvoidingLayoutData) -> Unit = {},
) {
    val emojiOnly = containsOnlyEmojisOrEmotes(content.text)
    val textStyle = when {
        emojiOnly -> ElementTheme.typography.fontHeadingXlRegular
        else -> ElementTheme.typography.scBubbleFont
    }
    val textColor = ElementTheme.colors.textPrimary
    val density = LocalDensity.current
    MatrixStyledFormattedText(
        content,
        color = textColor,
        style = if (content.inlineImages.isEmpty()) {
            textStyle.copy(textDirection = TextDirection.Content)
        } else {
            // Allow inline images/content to increase the line height on demand by having this unspecified here
            textStyle.copy(lineHeight = TextUnit.Unspecified, textDirection = TextDirection.Content)
        },
        modifier = modifier,
        formatter = LocalMatrixBodyFormatter.current ?: matrixBodyFormatter(),
        drawStyle = LocalMatrixBodyDrawStyle.current ?: matrixBodyDrawStyle(),
        onTextLayout = { result ->
            val lastLine = if (result.lineCount > 0) {
                result.getLineRight(result.lineCount - 1).roundToInt()
            } else {
                result.size.width
            }
            onContentLayoutChange(
                ContentAvoidingLayoutData(
                    contentWidth = result.size.width,
                    contentHeight = result.size.height,
                    nonOverlappingContentWidth = lastLine,
                    nonOverlappingContentHeight = density.run { textStyle.lineHeight.roundToPx() },
                )
            )
        },
        inlineContent = content.inlineImages.toInlineContent(
            density = LocalDensity.current,
            defaultHeight = textStyle.lineHeight,
            minWidth = MIN_IMAGE_WIDTH.dp,
            maxWidth = MAX_IMAGE_WIDTH.dp,
            minHeight = MIN_IMAGE_HEIGHT.dp,
            maxHeight = MAX_IMAGE_HEIGHT.dp,
        ) { info, modifier ->
            InlineImage(info, textStyle, textColor, modifier)
        },
        onLinkLongPress = { link ->
            (link as? LinkAnnotation.Url)?.url?.let { url ->
                onLinkLongClick(Link(url))
            }
        }
    )
}

@Composable
private fun InlineImage(
    info: InlineImageInfo,
    textStyle: TextStyle,
    textColor: Color,
    modifier: Modifier = Modifier,
) {
    SubcomposeAsyncImage(
        modifier = modifier,
        model = MediaRequestData(MediaSource(info.uri), MediaRequestData.Kind.Content),
        contentScale = ContentScale.Crop,
        alignment = Alignment.Center,
        contentDescription = info.alt ?: info.title,
    ) {
        AnimatedContent(
            painter.state.collectAsState().value,
            transitionSpec = {
                fadeIn(
                    animationSpec = tween(50)
                ) togetherWith fadeOut(
                    animationSpec = tween(50)
                )
            },
        ) { state ->
            when (state) {
                is AsyncImagePainter.State.Success -> SubcomposeAsyncImageContent(modifier)
                else -> {
                    Text(
                        info.alt ?: info.title ?: "\uFFFD",
                        modifier,
                        style = textStyle,
                        color = textColor,
                    )
                }
            }
        }
    }
}

@Composable
internal fun containsOnlyEmojisOrEmotes(text: AnnotatedString): Boolean {
    return remember(text) { text.toString().replace(" ", "").containsOnlyEmojis(50) }
}

/* TODO recpect inline images for new renderer
@Composable
internal fun containsOnlyEmojisOrEmotes(formattedBody: CharSequence?, body: String): Boolean {
    // Ignore custom emotes when not rendered
    val formattedWithInlineImages = formattedBody?.takeIf {
        !ScPrefs.LEGACY_MESSAGE_RENDERING.value()
    }
    return remember(formattedWithInlineImages, body) {
        val toCheck = if (formattedWithInlineImages is Spanned) {
            val inlineImageSpans = formattedWithInlineImages.getSpans<InlineImageSpan>()
            var toCheck = SpannableStringBuilder(formattedWithInlineImages)
            inlineImageSpans.forEach { span ->
                // Inline images that are not a custom emote do not count
                if (!span.isEmoticon) {
                    return@remember false
                }
                val start = toCheck.getSpanStart(span)
                val end = toCheck.getSpanEnd(span)
                if (start != -1 && end != -1) {
                    toCheck = toCheck.replace(start, end, "\uD83D\uDC22")
                }
            }
            toCheck.toString()
        } else {
            formattedBody?.toString() ?: body
        }
        toCheck.replace(" ", "").containsOnlyEmojis(50)
    }
}
 */
