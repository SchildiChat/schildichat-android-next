package io.element.android.features.messages.impl.timeline.components.event

import android.graphics.drawable.Animatable
import android.text.Spannable
import android.text.SpannableString
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmapOrNull
import androidx.core.text.getSpans
import chat.schildi.lib.compose.thenIf
import chat.schildi.lib.preferences.ScPrefs
import chat.schildi.lib.preferences.value
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Scale
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextBasedContent
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.ui.media.MediaRequestData
import io.element.android.libraries.matrix.ui.messages.LocalRoomMemberProfilesCache
import io.element.android.libraries.textcomposer.mentions.LocalMentionSpanTheme
import io.element.android.libraries.textcomposer.mentions.updateMentionStyles
import io.element.android.wysiwyg.view.spans.InlineImageSpan
import timber.log.Timber

private const val CUSTOM_EMOTE_SIZE = 32
private const val DEFAULT_IMAGE_WIDTH = CUSTOM_EMOTE_SIZE
private const val DEFAULT_IMAGE_HEIGHT = CUSTOM_EMOTE_SIZE
private const val MAX_IMAGE_WIDTH = 128
private const val MAX_IMAGE_HEIGHT = 128
private const val MIN_IMAGE_WIDTH = 8
private const val MIN_IMAGE_HEIGHT = 8

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

@Composable // SC: Copy from upstream code in the non-extension file, then added formattedContent override + inline image resolution
private fun getTextWithResolvedMentions(toFormat: CharSequence?, content: TimelineItemTextBasedContent): CharSequence {
    val userProfileCache = LocalRoomMemberProfilesCache.current
    val lastCacheUpdate by userProfileCache.lastCacheUpdate.collectAsState()
    val mentionSpanTheme = LocalMentionSpanTheme.current
    val formattedBody = toFormat ?: content.pillifiedBody
    val textWithMentions = remember(formattedBody, mentionSpanTheme, lastCacheUpdate) {
        updateMentionSpans(formattedBody, userProfileCache)
        mentionSpanTheme.updateMentionStyles(formattedBody)
        formattedBody
    }.resolveInlineImageSpans()
    return SpannableString(textWithMentions)
}

@Composable
fun CharSequence.resolveInlineImageSpans(): CharSequence {
    if (!ScPrefs.RENDER_INLINE_IMAGES.value()) {
        return this
    }
    val context = LocalContext.current
    val density = LocalDensity.current
    val inlineImageSpans = (this as? Spanned)?.getSpans<InlineImageSpan>() ?: return this
    val spansToReplace = inlineImageSpans.mapNotNull { inSpan ->
        val src = inSpan.src.takeIf { it.startsWith("mxc://") } ?: return@mapNotNull null
        val originWidth = if (inSpan.isEmoticon) CUSTOM_EMOTE_SIZE else inSpan.width ?: inSpan.height ?: DEFAULT_IMAGE_WIDTH
        val originHeight = if (inSpan.isEmoticon) CUSTOM_EMOTE_SIZE else inSpan.height ?: inSpan.width ?: DEFAULT_IMAGE_HEIGHT
        val width = density.run { originWidth.coerceIn(MIN_IMAGE_WIDTH, MAX_IMAGE_WIDTH).dp.roundToPx() }
        val height = density.run { originHeight.coerceIn(MIN_IMAGE_HEIGHT, MAX_IMAGE_HEIGHT).dp.roundToPx() }
        val painter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(context)
                .data(MediaRequestData(MediaSource(url = src), MediaRequestData.Kind.Content))
                .scale(Scale.FILL)
                .size(width, height)
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
                    ImageSpan(drawable)
                } else {
                    val bitmap = drawable.toBitmapOrNull()
                    if (bitmap == null) {
                        ImageSpan(drawable)
                    } else {
                        // Works a bit more reliable for some drawables, to not squeeze emotes when out of screen during initial render
                        ImageSpan(context, bitmap)
                    }
                }
                setSpan(span, start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            }
        }
    }
}
