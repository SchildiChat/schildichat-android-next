/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.customreaction

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import chat.schildi.lib.preferences.ScPrefs
import chat.schildi.lib.preferences.value
import io.element.android.emojibasebindings.Emoji
import io.element.android.features.messages.impl.emojis.RecentEmojiDataSource
import io.element.android.libraries.designsystem.theme.components.ModalBottomSheet
import io.element.android.libraries.designsystem.theme.components.hide
import io.element.android.libraries.matrix.api.timeline.item.event.EventOrTransactionId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomReactionBottomSheet(
    state: CustomReactionState,
    onSelectEmoji: (EventOrTransactionId, Emoji) -> Unit,
    onSelectCustomEmoji: (EventOrTransactionId, String) -> Unit, // SC
    recentEmojiDataSource: RecentEmojiDataSource?, // SC
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = ScPrefs.PREFER_FULLSCREEN_REACTION_SHEET.value())
    val coroutineScope = rememberCoroutineScope()
    val target = state.target as? CustomReactionState.Target.Success

    fun onDismiss() {
        state.eventSink(CustomReactionEvents.DismissCustomReactionSheet)
    }

    fun onEmojiSelectedDismiss(emoji: Emoji) {
        if (target?.event == null) return

        val wasSelected = state.selectedEmoji.contains(emoji.unicode)

        sheetState.hide(coroutineScope) {
            state.eventSink(CustomReactionEvents.DismissCustomReactionSheet)
            onSelectEmoji(target.event.eventOrTransactionId, emoji)

            if (!wasSelected) recentEmojiDataSource?.recordEmoji(emoji.unicode)
        }
    }

    fun onCustomEmojiSelectedDismiss(emoji: String) {
        if (target?.event == null) return

        val wasSelected = state.selectedEmoji.contains(emoji)

        sheetState.hide(coroutineScope) {
            state.eventSink(CustomReactionEvents.DismissCustomReactionSheet)
            onSelectCustomEmoji(target.event.eventOrTransactionId, emoji)

            if (!wasSelected) recentEmojiDataSource?.recordEmoji(emoji)
        }
    }

    if (target?.emojibaseStore != null && target.event.eventId != null) {
        ModalBottomSheet(
            onDismissRequest = ::onDismiss,
            sheetState = sheetState,
            modifier = modifier
        ) {
            EmojiPicker(
                onSelectEmoji = ::onEmojiSelectedDismiss,
                onSelectCustomEmoji = ::onCustomEmojiSelectedDismiss,
                emojibaseStore = target.emojibaseStore,
                selectedEmojis = state.selectedEmoji,
                recentEmojiDataSource = recentEmojiDataSource,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
