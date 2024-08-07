/*
 * Copyright (c) 2022 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.messages.impl.timeline.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import chat.schildi.theme.ScTheme
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.messages.impl.timeline.model.TimelineItemGroupPosition
import io.element.android.features.messages.impl.timeline.model.bubble.BubbleState
import io.element.android.features.messages.impl.timeline.model.bubble.BubbleStateProvider
import io.element.android.libraries.core.extensions.to01
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.text.toPx
import io.element.android.libraries.designsystem.theme.components.Surface
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.messageFromMeBackground
import io.element.android.libraries.designsystem.theme.messageFromOtherBackground
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.testtags.testTag

private val BUBBLE_RADIUS = 12.dp
internal val BUBBLE_INCOMING_OFFSET = 16.dp
private val avatarRadius = AvatarSize.TimelineSender.dp / 2

// Design says: The maximum width of a bubble is still 3/4 of the screen width. But try with 85% now.
private const val BUBBLE_WIDTH_RATIO = 0.85f

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageEventBubble(
    state: BubbleState,
    interactionSource: MutableInteractionSource,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {},
) {
    val isScTheme = ScTheme.yes
    val bubbleRadius = ScTheme.exposures.bubbleRadius
    fun bubbleShape(): Shape {
        if (isScTheme) return RoundedCornerShape(bubbleRadius)
        val topLeftCorner = if (state.cutTopStart) 0.dp else BUBBLE_RADIUS
        return when (state.groupPosition) {
            TimelineItemGroupPosition.First -> if (state.isMine) {
                RoundedCornerShape(BUBBLE_RADIUS, BUBBLE_RADIUS, 0.dp, BUBBLE_RADIUS)
            } else {
                RoundedCornerShape(topLeftCorner, BUBBLE_RADIUS, BUBBLE_RADIUS, 0.dp)
            }
            TimelineItemGroupPosition.Middle -> if (state.isMine) {
                RoundedCornerShape(BUBBLE_RADIUS, 0.dp, 0.dp, BUBBLE_RADIUS)
            } else {
                RoundedCornerShape(0.dp, BUBBLE_RADIUS, BUBBLE_RADIUS, 0.dp)
            }
            TimelineItemGroupPosition.Last -> if (state.isMine) {
                RoundedCornerShape(BUBBLE_RADIUS, 0.dp, BUBBLE_RADIUS, BUBBLE_RADIUS)
            } else {
                RoundedCornerShape(0.dp, BUBBLE_RADIUS, BUBBLE_RADIUS, BUBBLE_RADIUS)
            }
            TimelineItemGroupPosition.None ->
                RoundedCornerShape(
                    topLeftCorner,
                    BUBBLE_RADIUS,
                    BUBBLE_RADIUS,
                    BUBBLE_RADIUS
                )
        }
    }

    fun Modifier.offsetForItem(): Modifier {
        return when {
            state.isMine -> this
            state.timelineRoomInfo.isDm -> this
            else -> offset(x = BUBBLE_INCOMING_OFFSET)
        }
    }

    // Ignore state.isHighlighted for now, we need a design decision on it.
    val backgroundBubbleColor = when {
        ScTheme.yes && state.scIsBgLess -> Color.Transparent
        state.isMine -> ScTheme.exposures.bubbleBgOutgoing ?: ElementTheme.colors.messageFromMeBackground
        else -> ScTheme.exposures.bubbleBgIncoming ?: ElementTheme.colors.messageFromOtherBackground
    }
    val bubbleShape = bubbleShape()
    val radiusPx = (avatarRadius + SENDER_AVATAR_BORDER_WIDTH).toPx()
    val yOffsetPx = -(NEGATIVE_MARGIN_FOR_BUBBLE + avatarRadius).toPx()
    Box(
        modifier = modifier
            .fillMaxWidth(BUBBLE_WIDTH_RATIO)
            .padding(start = avatarRadius, end = 16.dp)
            .offsetForItem()
            .graphicsLayer {
                compositingStrategy = CompositingStrategy.Offscreen
            }
            .drawWithContent {
                drawContent()
                if (state.cutTopStart) {
                    drawCircle(
                        color = Color.Black,
                        center = Offset(
                            x = 0f,
                            y = yOffsetPx,
                        ),
                        radius = radiusPx,
                        blendMode = BlendMode.Clear,
                    )
                }
            },
        // Need to set the contentAlignment again (it's already set in TimelineItemEventRow), for the case
        // when content width is low.
        contentAlignment = if (state.isMine) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Surface(
            modifier = Modifier
                .testTag(TestTags.messageBubble)
                .widthIn(min = 80.dp)
                .clip(bubbleShape)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                    indication = rememberRipple(),
                    interactionSource = interactionSource
                ),
            color = backgroundBubbleColor,
            shape = bubbleShape,
            content = content
        )
    }
}

@PreviewsDayNight
@Composable
internal fun MessageEventBubblePreview(@PreviewParameter(BubbleStateProvider::class) state: BubbleState) = ElementPreview {
    // Due to position offset, surround with a Box
    Box(
        modifier = Modifier
            .size(width = 240.dp, height = 64.dp)
            .padding(vertical = 8.dp),
        contentAlignment = if (state.isMine) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        MessageEventBubble(
            state = state,
            interactionSource = remember { MutableInteractionSource() },
            onClick = {},
            onLongClick = {},
        ) {
            // Render the state as a text to better understand the previews
            Box(
                modifier = Modifier
                    .size(width = 120.dp, height = 32.dp)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "${state.groupPosition.javaClass.simpleName} m:${state.isMine.to01()} h:${state.isHighlighted.to01()}",
                    style = ElementTheme.typography.fontBodyXsRegular,
                )
            }
        }
    }
}
