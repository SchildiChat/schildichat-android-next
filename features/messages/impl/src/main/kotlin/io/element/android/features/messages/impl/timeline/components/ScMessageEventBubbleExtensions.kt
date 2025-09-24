package io.element.android.features.messages.impl.timeline.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import chat.schildi.theme.ScTheme
import io.element.android.features.messages.impl.timeline.model.bubble.BubbleState

@Composable
fun scMessageBubbleShape() = if (ScTheme.yes)
    RoundedCornerShape(ScTheme.exposures.bubbleRadius)
else
    null

@Composable
fun scMessageBubbleBg(state: BubbleState) = when {
    ScTheme.yes && state.scIsBgLess -> Color.Transparent
    state.isMine -> ScTheme.bubbleBgOutgoing
    else -> ScTheme.bubbleBgIncoming
}
