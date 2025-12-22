package io.element.android.libraries.designsystem.components.avatar.internal

import androidx.compose.runtime.Composable
import chat.schildi.lib.preferences.ScPrefs
import chat.schildi.lib.preferences.value
import io.element.android.libraries.designsystem.components.avatar.AvatarData

@Composable
fun AvatarData.scInitialLetters() = if (ScPrefs.TWO_INITIALS_AVATAR_FALLBACK.value())
    scPreviewLetters
else
    null
