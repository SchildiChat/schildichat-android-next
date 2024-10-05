package io.element.android.libraries.designsystem.colors

import androidx.compose.runtime.Composable
import chat.schildi.theme.ScTheme
import chat.schildi.theme.scAvatarColorsDark
import chat.schildi.theme.scAvatarColorsLight
import io.element.android.compound.theme.AvatarColors
import io.element.android.compound.theme.ElementTheme

@Composable
fun scAvatarColors(id: String): AvatarColors? {
    if (ScTheme.yes) {
        val colors = if (ElementTheme.isLightTheme) scAvatarColorsLight else scAvatarColorsDark
        return colors[id.toHash(colors.size)]
    }
    return null
}
