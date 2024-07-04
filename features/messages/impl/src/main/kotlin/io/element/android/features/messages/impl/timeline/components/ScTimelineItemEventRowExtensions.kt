package io.element.android.features.messages.impl.timeline.components

import androidx.compose.runtime.Composable
import chat.schildi.lib.preferences.ScPrefs
import chat.schildi.lib.preferences.value
import io.element.android.libraries.designsystem.colors.powerLevelToAvatarColors
import io.element.android.libraries.designsystem.components.avatar.AvatarData

@Composable
fun scAvatarColorsOverride(senderAvatar: AvatarData) = if (ScPrefs.PL_DISPLAY_NAME.value()) {
    powerLevelToAvatarColors(senderAvatar.powerLevel)
} else {
    null
}
