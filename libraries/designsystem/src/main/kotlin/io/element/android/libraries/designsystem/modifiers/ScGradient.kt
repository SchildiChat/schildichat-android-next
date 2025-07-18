package io.element.android.libraries.designsystem.modifiers

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import chat.schildi.theme.ScTheme
import io.element.android.compound.theme.ElementTheme

@Composable
fun scSubtleColorStops(): Array<Pair<Float, Color>>? = if (ScTheme.yes)
    arrayOf(
        0f to (ScTheme.exposures.messageHighlightBg ?: ElementTheme.colors.textActionAccent.copy(alpha = 0.5f)),
        0.75f to ElementTheme.colors.bgCanvasDefault,
        1f to Color.Transparent
    )
else
    null
