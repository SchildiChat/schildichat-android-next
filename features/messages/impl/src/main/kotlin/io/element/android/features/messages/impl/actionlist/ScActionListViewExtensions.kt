package io.element.android.features.messages.impl.actionlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import chat.schildi.lib.preferences.ScPrefs
import chat.schildi.lib.preferences.value
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.theme.components.Text

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

@Composable
fun ScMessageContextPreview(
    body: String,
): Unit? {
    if (!ScPrefs.MESSAGE_CONTEXT_MENU_TEXT_SELECTABLE.value()) return null
    val contentStyle = ElementTheme.typography.fontBodyMdRegular.copy(color = MaterialTheme.colorScheme.secondary)
    SelectionContainer(
        Modifier.heightIn(max = 80.dp).verticalScroll(rememberScrollState())
    ) {
        Text(
            body,
            style = contentStyle
        )
    }
    return Unit
}
