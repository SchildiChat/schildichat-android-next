package io.element.android.features.userprofile.shared

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString

@Composable
fun Modifier.copyTextOnLongPress(text: String): Modifier {
    val clipboard = LocalClipboardManager.current
    return this
        // Not using combinedClickable() since we don't use onClick() and thus don't want to render a ripple animation.
        .pointerInput(Unit){
            detectTapGestures(
                onLongPress = {
                    clipboard.setText(AnnotatedString(text))
                }
            )
        }
}
