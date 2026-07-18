package io.element.android.features.messages.impl.timeline.components.event

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import chat.schildi.matrixsdk.containsOnlyEmojis
import chat.schildi.theme.scBubbleFont
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.size.Size
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import com.beeper.android.messageformat.InlineImageInfo
import com.beeper.android.messageformat.MatrixBodyAnnotations
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
import kotlin.math.roundToInt

// All values in DP here
private const val MAX_IMAGE_WIDTH = 250
private const val MAX_IMAGE_HEIGHT = 300
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
    val emojiOnly = containsOnlyEmojisOrEmotes(content.text, content.inlineImages)
    val textStyle = when {
        emojiOnly -> ElementTheme.typography.fontHeadingXlRegular
        else -> ElementTheme.typography.scBubbleFont
    }
    val textColor = ElementTheme.colors.textPrimary
    val density = LocalDensity.current
    val actualImageSizes = remember(content.inlineImages) { mutableStateMapOf<String, IntSize>() }
    MatrixStyledFormattedText(
        content,
        color = textColor,
        style = if (content.inlineImages.isEmpty() && content.inlineTables.isEmpty()) {
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
        inlineContent = { result ->
            result.inlineImages.toInlineContent(
                density = LocalDensity.current,
                defaultHeight = textStyle.lineHeight,
                minWidth = MIN_IMAGE_WIDTH.dp,
                maxWidth = MAX_IMAGE_WIDTH.dp,
                minHeight = MIN_IMAGE_HEIGHT.dp,
                maxHeight = MAX_IMAGE_HEIGHT.dp,
                actualImageSizes = actualImageSizes,
            ) { info, modifier ->
                InlineImage(
                    info = info,
                    textStyle = textStyle,
                    textColor = textColor,
                    modifier = modifier,
                    onPainterSuccess = { result -> actualImageSizes[info.uri] = IntSize(result.image.width, result.image.height) },
                )
            }
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
    onPainterSuccess: (SuccessResult) -> Unit = {},
) {
    val context = LocalContext.current
    val model = remember(info) {
        val data = MediaRequestData(MediaSource(info.uri), MediaRequestData.Kind.Content)
        // Non-square custom emotes may unexpectedly resize once we know the measures, and become blurry.
        // For huge inline images it's probably safer to keep not loading the full original size.
        if (info.isEmote && info.width == null) {
            ImageRequest.Builder(context)
                .data(data)
                .size(Size.ORIGINAL)
                .build()
        } else {
            data
        }
    }
    SubcomposeAsyncImage(
        modifier = modifier,
        model = model,
        contentScale = ContentScale.Fit,
        alignment = Alignment.Center,
        contentDescription = info.alt ?: info.title,
    ) {
        val painterState = painter.state.collectAsState().value
        AnimatedContent(
            painterState,
            transitionSpec = {
                fadeIn(
                    animationSpec = tween(50)
                ) togetherWith fadeOut(
                    animationSpec = tween(50)
                )
            },
        ) { state ->
            when (state) {
                is AsyncImagePainter.State.Success -> {
                    SubcomposeAsyncImageContent(Modifier)
                    LaunchedEffect(state.result) {
                        onPainterSuccess(state.result)
                    }
                }
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
internal fun containsOnlyEmojisOrEmotes(text: AnnotatedString, inlineImages: Map<String, InlineImageInfo>): Boolean {
    return remember(text) { text.containsOnlyEmojis(inlineImages, 50) }
}

fun AnnotatedString.containsOnlyEmojis(
    inlineImages: Map<String, InlineImageInfo> = emptyMap(),
    maxEmojis: Int = Integer.MAX_VALUE,
): Boolean {
    return if (inlineImages.isNotEmpty()) {
        if (inlineImages.values.any { !it.isEmote }) {
            return false
        }
        // If all is custom emotes, those also count as emojis.
        val customEmotes = getStringAnnotations(MatrixBodyAnnotations.INLINE_IMAGE, 0, length)
            .sortedByDescending { it.start }
        var toCheck: CharSequence = this
        customEmotes.forEach {
            toCheck = toCheck.removeRange(it.start, it.end)
        }
        toCheck.toString().replace(" ", "").containsOnlyEmojisOrIsEmpty(maxEmojis - customEmotes.size)
    } else {
        toString().replace(" ", "").containsOnlyEmojis(maxEmojis)
    }
}

fun String.containsOnlyEmojisOrIsEmpty(maxEmojis: Int = Integer.MAX_VALUE, throwOnError: Boolean = false) = isEmpty() || containsOnlyEmojis(maxEmojis, throwOnError)
