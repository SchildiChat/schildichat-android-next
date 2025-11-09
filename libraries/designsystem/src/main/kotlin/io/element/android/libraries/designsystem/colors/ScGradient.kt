package io.element.android.libraries.designsystem.colors

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import chat.schildi.theme.ScTheme
import io.element.android.compound.theme.ElementTheme

@Composable
@ReadOnlyComposable
fun scGradientActionColors(): List<Color>? = if (ScTheme.yes) listOf(
    // We don't actually want a gradient, but to use with gradient modifiers we still need to provide two colors
    ElementTheme.colors.textActionAccent,
    ElementTheme.colors.textActionAccent,
) else null

@Composable
@ReadOnlyComposable
fun scGradientSubtleColors(): List<Color>? = if (ScTheme.yes)
    (ScTheme.exposures.messageHighlightBg ?: ElementTheme.colors.textActionAccent.copy(alpha = 0.5f)).let {
        // We don't actually want a gradient, but to use with gradient modifiers we still need to provide two colors
        listOf(it, it)
    }
else null

@Composable
@ReadOnlyComposable
fun scGradientInfoColors(): List<Color>? = null
