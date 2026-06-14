package io.element.android.libraries.textcomposer.components

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import chat.schildi.lib.preferences.ScPrefs
import chat.schildi.lib.preferences.value
import chat.schildi.theme.ScTheme
import io.element.android.compound.theme.ElementTheme

@Composable
fun scSendButtonBackgroundModifier(canSendMessage: Boolean) = when {
    !ScTheme.yes -> null
    canSendMessage -> Modifier.background(ElementTheme.colors.bgAccentRest)
    else -> Modifier
}

@Composable
fun scSendButtonTint() = when {
    !ScTheme.yes -> null
    else -> ScTheme.exposures.colorOnAccent
}

/**
 * Whether the voice message record button should be shown in the composer's end-button slot.
 * SchildiChat lets users hide it (issue #99) to avoid recording by accident.
 */
@Composable
fun scShowVoiceRecorderButton(): Boolean = !ScPrefs.HIDE_VOICE_MESSAGE_BUTTON.value()
