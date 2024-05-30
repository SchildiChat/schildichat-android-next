package io.element.android.libraries.designsystem.components.button

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import chat.schildi.theme.ScTheme
import io.element.android.compound.theme.ElementTheme

@Composable
fun scSuperButtonColors(): List<Color>? = if (ScTheme.yes)
    listOf(ElementTheme.colors.textActionAccent, ElementTheme.colors.textActionAccent)
else
    null
