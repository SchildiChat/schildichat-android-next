package io.element.android.libraries.designsystem.theme.components

import androidx.compose.material3.ListItemColors
import androidx.compose.runtime.Composable
import chat.schildi.theme.ScTheme
import io.element.android.compound.theme.ElementTheme

@Composable
fun ListItemColors.scDisabledSupportingTextColor() = if (ScTheme.yes) supportingTextColor.let {
    it.copy(alpha = it.alpha * ElementTheme.colors.textDisabled.alpha)
} else null
