package io.element.android.features.messages.impl.timeline.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import chat.schildi.theme.ScTheme
import io.element.android.compound.theme.ElementTheme

@Composable
fun scMessageHighlightColors(): List<Color>? = if (ScTheme.yes)
    listOf(
        ScTheme.exposures.messageHighlightBg ?: ElementTheme.colors.textActionAccent.copy(alpha = 0.5f),
        Color.Transparent
    )
else
    null
