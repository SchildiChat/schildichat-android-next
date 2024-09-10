/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.event

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemStickerContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemStickerContentProvider
import io.element.android.libraries.designsystem.components.blurhash.BlurHashAsyncImage
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.matrix.ui.media.MediaRequestData

//private const val STICKER_SIZE_IN_DP = 120
private const val DEFAULT_ASPECT_RATIO = 1.33f

@Composable
fun TimelineItemStickerView(
    content: TimelineItemStickerContent,
    modifier: Modifier = Modifier,
) {
    val safeAspectRatio = content.aspectRatio ?: DEFAULT_ASPECT_RATIO
    Box(
        modifier = modifier
            .heightIn(min = 120.dp, max = 192.dp)
            .widthIn(max = 216.dp)
            .aspectRatio(safeAspectRatio, false),
        contentAlignment = Alignment.TopStart,
    ) {
        BlurHashAsyncImage(
            model = MediaRequestData(content.preferredMediaSource, MediaRequestData.Kind.File(content.body, content.mimeType)),
            blurHash = content.blurhash,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemStickerViewPreview(@PreviewParameter(TimelineItemStickerContentProvider::class) content: TimelineItemStickerContent) = ElementPreview {
    TimelineItemStickerView(content)
}
