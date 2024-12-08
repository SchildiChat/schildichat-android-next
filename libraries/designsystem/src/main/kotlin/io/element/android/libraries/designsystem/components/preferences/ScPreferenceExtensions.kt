package io.element.android.libraries.designsystem.components.preferences

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import chat.schildi.theme.ScTheme

@Composable // We DO NOT want to override the TextColor from ListItem!!!!
fun scUnspecifiedColor() = if (ScTheme.yes) Color.Unspecified else null
