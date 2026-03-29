package io.element.android.libraries.matrix.ui.messages.reply

import androidx.compose.runtime.compositionLocalOf

// Just want to know if a given view is in the composer content or not for now.
val LocalIsInComposer = compositionLocalOf<Boolean> { false }
