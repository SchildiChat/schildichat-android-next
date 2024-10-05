package io.element.android.features.messages.impl.actionlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
inline fun ScQuickEmojiRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    crossinline content: @Composable RowScope.(Int) -> Unit
) {
    BoxWithConstraints {
        // Emojis are 48.dp right now in ActionListView.kt.
        // Subtract one for the extra button to open the full picker.
        val emojiCount = (maxWidth / 52.dp).toInt() - 1
        Row(
            modifier = modifier,
            horizontalArrangement = horizontalArrangement,
            verticalAlignment = verticalAlignment,
            content = {
                content(emojiCount)
            }
        )
    }
}
