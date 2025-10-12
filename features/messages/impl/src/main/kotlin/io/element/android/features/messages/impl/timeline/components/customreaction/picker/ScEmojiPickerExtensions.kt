package io.element.android.features.messages.impl.timeline.components.customreaction.picker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import chat.schildi.lib.preferences.ScPrefs
import chat.schildi.lib.preferences.value
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.launch

@Composable
fun EmojiPickerState.scEmojiPickerSize() = categories.size + 1 + (if (ScPrefs.ALWAYS_SHOW_REACTION_SEARCH_BAR.value()) 0 else 1)
fun EmojiPickerState.pageFreeformReactionIndex() = categories.size
fun EmojiPickerState.pageSearchIndex() = categories.size + 1
fun Int.removeScPickerOffset() = this //- 1
fun Int.addScPickerOffset() = this //+ 1

@Composable
fun ScEmojiPickerTabsEnd(
    state: EmojiPickerState,
    pagerState: PagerState,
    launchSearch: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    Tab(
        icon = {
            Icon(
                imageVector = Icons.Default.TextFields,
                contentDescription = stringResource(id = R.string.sc_freeform_reaction),
            )
        },
        selected = pagerState.currentPage == state.pageFreeformReactionIndex(),
        onClick = {
            coroutineScope.launch { pagerState.animateScrollToPage(state.pageFreeformReactionIndex()) }
        }
    )
    if (!ScPrefs.ALWAYS_SHOW_REACTION_SEARCH_BAR.value()) {
        Tab(
            icon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(id = io.element.android.libraries.ui.strings.R.string.action_search),
                )
            },
            selected = pagerState.currentPage == state.pageSearchIndex(),
            onClick = launchSearch,
        )
    }
}

@Composable
fun scEmojiPickerPage(
    state: EmojiPickerState,
    index: Int,
    selectedIndex: Int,
    onSelectCustomEmoji: (String) -> Unit,
    launchSearch: () -> Unit,
): Boolean {
    return when (index) {
        state.pageFreeformReactionIndex() -> {
            val text = remember { mutableStateOf("") }
            val focusRequester = remember { FocusRequester() }
            val focusManager = LocalFocusManager.current
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp), verticalAlignment = Alignment.Top) {
                TextField(
                    value = text.value,
                    onValueChange = { text.value = it },
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .weight(1f, fill = true),
                    singleLine = true,
                    label = {
                        Text(stringResource(R.string.sc_freeform_reaction),)
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { onSelectCustomEmoji(text.value) })
                )
                SendButton(Modifier.align(Alignment.Bottom)) {
                    onSelectCustomEmoji(text.value)
                }
            }
            LaunchedEffect(selectedIndex == state.pageFreeformReactionIndex()) {
                if (selectedIndex == state.pageFreeformReactionIndex()) {
                    focusRequester.requestFocus()
                } else {
                    focusManager.clearFocus()
                }
            }
            true
        }
        state.pageSearchIndex() -> {
            // Shouldn't be reachable by clicking the search icon, but still by swiping
            if (selectedIndex == index) {
                LaunchedEffect(Unit) { launchSearch() }
            }
            true
        }
        else -> false
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
