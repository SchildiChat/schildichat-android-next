package io.element.android.libraries.designsystem.theme.components

import androidx.compose.runtime.Composable
import chat.schildi.theme.ScTheme
import io.element.android.compound.theme.ElementTheme

@Composable
fun ListItemStyle.supportingTextColorWithEnabled(enabled: Boolean) = this.supportingTextColor().let {
    if (enabled || !ScTheme.yes) {
        it
    } else {
        it.copy(
            alpha = it.alpha * ElementTheme.colors.textDisabled.alpha,
        )
    }
}
