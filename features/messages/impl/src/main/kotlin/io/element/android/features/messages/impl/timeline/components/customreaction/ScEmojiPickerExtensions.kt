package io.element.android.features.messages.impl.timeline.components.customreaction

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Tab
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import chat.schildi.lib.R
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.emojibasebindings.EmojibaseCategory
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextField
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.launch

val SC_EMOJI_PICKER_SIZE = EmojibaseCategory.entries.size + 1
val PAGE_FREEFORM_REACTION = EmojibaseCategory.entries.size

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScEmojiPickerTabs(pagerState: PagerState) {
    val coroutineScope = rememberCoroutineScope()
    Tab(
        icon = {
            Icon(
                imageVector = Icons.Default.TextFields,
                contentDescription = stringResource(id = R.string.sc_freeform_reaction),
            )
        },
        selected = pagerState.currentPage == PAGE_FREEFORM_REACTION,
        onClick = {
            coroutineScope.launch { pagerState.animateScrollToPage(PAGE_FREEFORM_REACTION) }
        }
    )
}

@Composable
fun scEmojiPickerPage(index: Int, selectedIndex: Int, onCustomEmojiSelected: (String) -> Unit): Boolean {
    return if (index == PAGE_FREEFORM_REACTION) {
        val text = remember { mutableStateOf("") }
        val focusRequester = remember { FocusRequester() }
        val focusManager = LocalFocusManager.current
        Row(Modifier.fillMaxSize().padding(8.dp), verticalAlignment = Alignment.Top) {
            TextField(
                value = text.value,
                onValueChange = { text.value = it },
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .weight(1f, fill = true),
                singleLine = true,
                label = {
                    Text(stringResource(R.string.sc_freeform_reaction))
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                ),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onCustomEmojiSelected(text.value) })
            )
            SendButton {
                onCustomEmojiSelected(text.value)
            }
        }
        LaunchedEffect(selectedIndex == PAGE_FREEFORM_REACTION) {
            if (selectedIndex == PAGE_FREEFORM_REACTION) {
                focusRequester.requestFocus()
            } else {
                focusManager.clearFocus()
            }
        }
        true
    } else {
        false
    }
}

// More or less a copy of upstream's SendButton which is not available here
@Composable
private fun SendButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    IconButton(
        modifier = modifier
            .size(48.dp),
        onClick = onClick,
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .size(36.dp)
                .background(ElementTheme.colors.iconAccentTertiary)
        ) {
            Icon(
                modifier = Modifier
                    .padding(start = 2.dp)
                    .align(Alignment.Center),
                imageVector = CompoundIcons.Send(),
                contentDescription = stringResource(CommonStrings.action_send),
                tint = Color.White,
            )
        }
    }
}
