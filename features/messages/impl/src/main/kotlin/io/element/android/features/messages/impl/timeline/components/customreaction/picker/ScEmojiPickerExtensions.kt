package io.element.android.features.messages.impl.timeline.components.customreaction.picker

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import io.element.android.emojibasebindings.Emoji
import io.element.android.emojibasebindings.EmojibaseCategory
import io.element.android.features.messages.impl.emojis.RecentEmojiDataSource
import io.element.android.features.messages.impl.timeline.components.customreaction.EmojiItem
import io.element.android.libraries.designsystem.text.toSp
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
fun scEmojiPickerSize() = EmojibaseCategory.entries.size + 2 + (if (ScPrefs.ALWAYS_SHOW_REACTION_SEARCH_BAR.value()) 0 else 1)
const val PAGE_RECENT_EMOJI = 0
val PAGE_FREEFORM_REACTION = EmojibaseCategory.entries.size + 1
val PAGE_SEARCH = EmojibaseCategory.entries.size + 2
fun Int.removeScPickerOffset() = this - 1
fun Int.addScPickerOffset() = this + 1

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScEmojiPickerTabsStart(pagerState: PagerState) {
    val coroutineScope = rememberCoroutineScope()
    Tab(
        icon = {
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = stringResource(id = R.string.sc_recent_reaction),
            )
        },
        selected = pagerState.currentPage == PAGE_RECENT_EMOJI,
        onClick = {
            coroutineScope.launch { pagerState.animateScrollToPage(PAGE_RECENT_EMOJI) }
        }
    )
}

@Composable
fun ScEmojiPickerTabsEnd(pagerState: PagerState, launchSearch: () -> Unit) {
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
    if (!ScPrefs.ALWAYS_SHOW_REACTION_SEARCH_BAR.value()) {
        Tab(
            icon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(id = io.element.android.libraries.ui.strings.R.string.action_search),
                )
            },
            selected = pagerState.currentPage == PAGE_SEARCH,
            onClick = launchSearch,
        )
    }
}

@Composable
fun scEmojiPickerPage(
    index: Int,
    selectedIndex: Int,
    selectedEmojis: ImmutableSet<String>,
    recentEmojiDataSource: RecentEmojiDataSource?,
    onSelectCustomEmoji: (String) -> Unit,
    launchSearch: () -> Unit,
): Boolean {
    return when (index) {
        PAGE_RECENT_EMOJI -> {
            if (recentEmojiDataSource == null) {
                // Should only happen for test cases
                Timber.e("Missing recent emoji data source")
                return true
            }
            LaunchedEffect(recentEmojiDataSource) {
                recentEmojiDataSource.refresh(this)
            }
            val recentEmojis = recentEmojiDataSource.recentEmojis.collectAsState().value
            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
                columns = GridCells.Adaptive(minSize = 48.dp),
                contentPadding = PaddingValues(vertical = 10.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(recentEmojis, key = { it }) { item ->
                    EmojiItem(
                        modifier = Modifier.aspectRatio(1f),
                        item = Emoji("", "", null, persistentListOf(), item, null),
                        isSelected = selectedEmojis.contains(item),
                        onSelectEmoji = { onSelectCustomEmoji(it.unicode) },
                        emojiSize = 32.dp.toSp(),
                    )
                }
            }
            true
        }
        PAGE_FREEFORM_REACTION -> {
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
            LaunchedEffect(selectedIndex == PAGE_FREEFORM_REACTION) {
                if (selectedIndex == PAGE_FREEFORM_REACTION) {
                    focusRequester.requestFocus()
                } else {
                    focusManager.clearFocus()
                }
            }
            true
        }
        PAGE_SEARCH -> {
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
