package io.element.android.features.roommembermoderation.impl

import android.content.ClipData
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import kotlinx.coroutines.launch

@Composable
fun Modifier.copyTextOnLongPress(text: String): Modifier {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    return this
        // Not using combinedClickable() since we don't use onClick() and thus don't want to render a ripple animation.
        .pointerInput(Unit){
            detectTapGestures(
                onLongPress = {
                    scope.launch {
                        clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("mxid", text)))
                    }
                }
            )
        }
}
