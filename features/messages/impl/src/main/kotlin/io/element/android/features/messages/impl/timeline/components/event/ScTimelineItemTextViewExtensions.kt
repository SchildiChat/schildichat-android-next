package io.element.android.features.messages.impl.timeline.components.event

import android.graphics.drawable.Animatable
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ImageSpan
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmapOrNull
import androidx.core.text.getSpans
import chat.schildi.lib.compose.thenIf
import chat.schildi.lib.preferences.ScPrefs
import chat.schildi.lib.preferences.value
import chat.schildi.matrixsdk.containsOnlyEmojis
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Dimension
import coil.size.Precision
import coil.size.Scale
import coil.size.Size
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextBasedContent
import io.element.android.libraries.designsystem.text.roundToPx
import io.element.android.libraries.designsystem.text.toDp
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.ui.media.MediaRequestData
import io.element.android.libraries.matrix.ui.messages.LocalRoomMemberProfilesCache
import io.element.android.libraries.textcomposer.mentions.LocalMentionSpanTheme
import io.element.android.libraries.textcomposer.mentions.updateMentionStyles
import io.element.android.wysiwyg.view.spans.InlineImageSpan
import timber.log.Timber

// All values in DP here
private const val MAX_IMAGE_WIDTH = 128
private const val MAX_IMAGE_HEIGHT = 128
private const val MIN_IMAGE_WIDTH = 8
private const val MIN_IMAGE_HEIGHT = 8
private const val CUSTOM_EMOTE_FONT_SIZE_ADD = 4

// Seems like ALIGN_BOTTOM matches real emojis better than ALIGN_CENTER (which only works on recent Androids anyway),
// not considering ALIGN_BASELINE which can lead to cut images apparently (and is also worth for aligning to emojis).
private const val INLINE_IMAGE_ALIGN = ImageSpan.ALIGN_BOTTOM

@Composable
internal fun scGetTextWithResolvedMentions(
    content: TimelineItemTextBasedContent,
    collapsed: MutableState<Boolean>,
    textStyle: TextStyle,
): CharSequence {
    val canCollapse = content.formattedCollapsedBody != null
    val formattedBody = if (collapsed.value && canCollapse) content.formattedCollapsedBody else content.formattedBody
    return getTextWithResolvedMentions(
        toFormat = formattedBody,
        content = content,
        textStyle = textStyle,
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

@Composable // SC: Copy from upstream code in the non-extension file, then added formattedContent override + inline image resolution
private fun getTextWithResolvedMentions(
    toFormat: CharSequence?,
    content: TimelineItemTextBasedContent,
    textStyle: TextStyle,
): CharSequence {
    val userProfileCache = LocalRoomMemberProfilesCache.current
    val lastCacheUpdate by userProfileCache.lastCacheUpdate.collectAsState()
    val mentionSpanTheme = LocalMentionSpanTheme.current
    val formattedBody = toFormat ?: content.pillifiedBody
    val textWithMentions = remember(formattedBody, mentionSpanTheme, lastCacheUpdate) {
        updateMentionSpans(formattedBody, userProfileCache)
        mentionSpanTheme.updateMentionStyles(formattedBody)
        formattedBody
    }.resolveInlineImageSpans(textStyle)
    return SpannableString(textWithMentions)
}

@Composable
fun CharSequence.resolveInlineImageSpans(textStyle: TextStyle): CharSequence {
    if (!ScPrefs.RENDER_INLINE_IMAGES.value()) {
        return this
    }
    val context = LocalContext.current
    val inlineImageSpans = (this as? Spanned)?.getSpans<InlineImageSpan>() ?: return this
    val spansToReplace = inlineImageSpans.mapNotNull { inSpan ->
        val src = inSpan.src.takeIf { it.startsWith("mxc://") } ?: return@mapNotNull null
        val size = if (inSpan.isEmoticon) {
            Size(Dimension.Undefined, textStyle.customEmoteSize())
        } else {
            if (inSpan.width == null && inSpan.height == null) {
                Size(Dimension.Undefined, textStyle.customEmoteSize())
            } else {
                Size(
                    inSpan.width?.coerceIn(MIN_IMAGE_WIDTH, MAX_IMAGE_WIDTH)?.dp?.roundToPx()?.let { Dimension.Pixels(it) } ?: Dimension.Undefined,
                    inSpan.height?.coerceIn(MIN_IMAGE_HEIGHT, MAX_IMAGE_HEIGHT)?.dp?.roundToPx()?.let { Dimension.Pixels(it) } ?: Dimension.Undefined,
                )
            }
        }
        val painter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(context)
                .data(MediaRequestData(MediaSource(url = src), MediaRequestData.Kind.Content))
                .scale(Scale.FIT)
                .size(size)
                .precision(Precision.EXACT) // InlineImage uses original size, so we need to get this right
                .build()
        )
        LaunchedEffect(painter.state) {
            val state = painter.state
            if (state is AsyncImagePainter.State.Error) {
                Timber.tag("InlineImage").e(state.result.throwable, "Inline image failed to query \"$src\"")
            } else {
                Timber.tag("InlineImage").v("Inline image \"$src\" state is $state")
            }
        }
        val drawable = (painter.state as? AsyncImagePainter.State.Success)?.result?.drawable ?: return@mapNotNull null
        Pair(inSpan, drawable)
    }
    if (spansToReplace.isEmpty()) {
        return this
    }
    return remember(this, spansToReplace) {
        SpannableString(this).apply {
            spansToReplace.forEach { (inSpan, drawable) ->
                val start = getSpanStart(inSpan).takeIf { it != -1 } ?: return@forEach
                val end = getSpanEnd(inSpan).takeIf { it != -1 } ?: return@forEach
                val span = if (drawable is Animatable) {
                    ImageSpan(drawable, INLINE_IMAGE_ALIGN)
                } else {
                    val bitmap = drawable.toBitmapOrNull()
                    if (bitmap == null) {
                        ImageSpan(drawable, INLINE_IMAGE_ALIGN)
                    } else {
                        // Works a bit more reliable for some drawables, to not squeeze emotes when out of screen during initial render
                        ImageSpan(context, bitmap, INLINE_IMAGE_ALIGN)
                    }
                }
                setSpan(span, start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            }
        }
    }
}

@Composable
private fun TextStyle.customEmoteSize() = (fontSize.toDp() + CUSTOM_EMOTE_FONT_SIZE_ADD.dp).roundToPx()

@Composable
internal fun containsOnlyEmojisOrEmotes(formattedBody: CharSequence?, body: String): Boolean {
    // Ignore custom emotes when not rendered
    val formattedWithInlineImages = formattedBody?.takeIf {
        ScPrefs.RENDER_INLINE_IMAGES.value()
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

@Composable
fun scLinkLongClickListener(): (String) -> Unit {
    val clipboard = LocalClipboardManager.current
    return { clipboard.setText(AnnotatedString(it)) }
}
