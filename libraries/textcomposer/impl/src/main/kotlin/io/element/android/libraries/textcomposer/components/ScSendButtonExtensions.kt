package io.element.android.libraries.textcomposer.components

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import chat.schildi.theme.ScTheme
import io.element.android.compound.theme.ElementTheme

@Composable
fun scButtonBackgroundModifier() = if (ScTheme.yes) Modifier.background(ElementTheme.colors.bgAccentRest) else null
