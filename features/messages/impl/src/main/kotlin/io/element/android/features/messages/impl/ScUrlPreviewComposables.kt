package io.element.android.features.messages.impl

import android.app.Activity
import android.text.Spanned
import android.text.style.URLSpan
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.core.text.getSpans
import chat.schildi.lib.compose.thenIf
import chat.schildi.lib.preferences.ScPrefs
import chat.schildi.lib.preferences.value
import chat.schildi.matrixsdk.urlpreview.UrlPreview
import chat.schildi.matrixsdk.urlpreview.UrlPreviewInfo
import chat.schildi.matrixsdk.urlpreview.UrlPreviewProvider
import chat.schildi.theme.scBubbleFont
import coil.compose.AsyncImage
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.messages.impl.timeline.di.LocalUrlPreviewProvider
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextBasedContent
import io.element.android.libraries.androidutils.browser.openUrlInChromeCustomTab
import io.element.android.libraries.androidutils.system.openUrlInExternalApp
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.ui.media.MediaRequestData

@Composable
fun UrlPreviewProvider.takeIfEnabledForRoom(room: MatrixRoom): UrlPreviewProvider? {
    val allowed = if (room.isEncrypted) {
        ScPrefs.URL_PREVIEWS_IN_E2EE_ROOMS.value()
    } else {
        ScPrefs.URL_PREVIEWS.value()
    }
    return if (allowed) this else null
}

@Composable
fun resolveUrlPreview(content: TimelineItemTextBasedContent): UrlPreviewInfo? {
    // This will be null when url previews are disabled for this room
    val urlPreviewProvider = LocalUrlPreviewProvider.current ?: return null
    val preview = remember { mutableStateOf<UrlPreviewInfo?>(null) }
    LaunchedEffect(content) {
        val urls = (content.formattedBody as? Spanned)?.getSpans<URLSpan>()?.filter { it.url.startsWith("https://") }
        urls?.firstOrNull()?.url?.let { url ->
            urlPreviewProvider.fetchPreview(url) {
                preview.value = it?.let { UrlPreviewInfo(url, it) }
            }
        }
    }
    return preview.value
}

@Composable
fun UrlPreviewView(
    content: TimelineItemEventContent,
    skipTopPadding: Boolean,
) {
    val textBasedContent = content as? TimelineItemTextBasedContent ?: return
    val paddingValues = PaddingValues(
        start = 8.dp,
        end = 8.dp,
        top = if (skipTopPadding) 0.dp else 8.dp,
        bottom = 0.dp,
    )
    UrlPreviewViewForContent(textBasedContent, paddingValues)
}

@Composable
fun UrlPreviewViewForContent(
    content: TimelineItemTextBasedContent,
    paddingValues: PaddingValues,
) {
    val (url, urlPreview) = resolveUrlPreview(content) ?: return
    if (urlPreview.imageUrl == null && urlPreview.title == null && urlPreview.description == null) {
        return
    }
    val context = LocalContext.current
    val openLinksInCustomTab = ScPrefs.OPEN_LINKS_IN_CUSTOM_TAB.value()
    val isDark = ElementTheme.isLightTheme.not()
    UrlPreviewView(urlPreview, paddingValues) {
        if (openLinksInCustomTab && context is Activity) {
            context.openUrlInChromeCustomTab(null, isDark, url)
        } else {
            context.openUrlInExternalApp(url)
        }
    }
}

@Composable
fun UrlPreviewView(
    urlPreview: UrlPreview,
    paddingValues: PaddingValues,
    onClick: () -> Unit
) {
    Column(
        Modifier
            // Similar background design to InReplyToView
            .padding(paddingValues)
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, ElementTheme.colors.textDisabled, RoundedCornerShape(6.dp)) // Add border so link previews look different from replies
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        val titleColumnHeight = remember { mutableIntStateOf(0) }
        val density = LocalDensity.current
        Row {
            urlPreview.imageUrl?.let { imageUrl ->
                AsyncImage(
                    modifier = Modifier
                        .sizeIn(
                            maxWidth = 140.dp,
                            maxHeight = max(80.dp, density.run { titleColumnHeight.intValue.toDp() }),
                        )
                        .padding(end = 4.dp, top = 4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .align(Alignment.Top),
                    model = MediaRequestData(MediaSource(imageUrl), MediaRequestData.Kind.Content),
                    contentScale = ContentScale.Fit,
                    alignment = Alignment.Center,
                    contentDescription = null,
                )
            }
            Column(
                Modifier
                    .padding(horizontal = 4.dp)
                    .align(Alignment.CenterVertically)
                    .onGloballyPositioned { titleColumnHeight.intValue = it.size.height },
            ) {
                urlPreview.title?.let { title ->
                    Text(
                        text = title,
                        style = ElementTheme.typography.scBubbleFont,
                        color = ElementTheme.colors.textPrimary,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                urlPreview.siteName?.let { site ->
                    Text(
                        text = site,
                        style = ElementTheme.typography.scBubbleFont,
                        color = ElementTheme.colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        urlPreview.description?.let { description ->
            val sanitized = description.replace("\n\n", "\n").replace("\n", " ")
            var expanded by remember { mutableStateOf(false) }
            Text(
                text = sanitized,
                style = ElementTheme.typography.scBubbleFont,
                color = ElementTheme.colors.textSecondary,
                maxLines = if (expanded) 50 else 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .thenIf(urlPreview.imageUrl != null || urlPreview.title != null || urlPreview.siteName != null) { padding(top = 4.dp) }
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 4.dp),
            )
        }
    }
}

@Preview
@Composable
fun UrlPreviewViewPreview() {
    UrlPreviewView(
        UrlPreview(
            //imageUrl = "asdf",
            title = "Title",
            description = "Description",
            siteName = "Site",
        ),
        PaddingValues(8.dp)
    ) {}
}
