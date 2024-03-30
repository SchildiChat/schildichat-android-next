/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import io.element.android.libraries.matrix.api.core.EventId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomReactionBottomSheet(
    state: CustomReactionState,
    onEmojiSelected: (EventId, Emoji) -> Unit,
    onCustomEmojiSelected: (EventId, String) -> Unit,
    recentEmojiDataSource: RecentEmojiDataSource?,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = ScPrefs.PREFER_FULLSCREEN_REACTION_SHEET.value())
    val coroutineScope = rememberCoroutineScope()
    val target = state.target as? CustomReactionState.Target.Success

    fun onDismiss() {
        state.eventSink(CustomReactionEvents.DismissCustomReactionSheet)
    }

    fun onEmojiSelectedDismiss(emoji: Emoji) {
        if (target?.event?.eventId == null) return

        val wasSelected = state.selectedEmoji.contains(emoji.unicode)

        sheetState.hide(coroutineScope) {
            state.eventSink(CustomReactionEvents.DismissCustomReactionSheet)
            onEmojiSelected(target.event.eventId, emoji)

            if (!wasSelected) recentEmojiDataSource?.recordEmoji(emoji.unicode)
        }
    }

    fun onCustomEmojiSelectedDismiss(emoji: String) {
        if (target?.event?.eventId == null) return

        val wasSelected = state.selectedEmoji.contains(emoji)

        sheetState.hide(coroutineScope) {
            state.eventSink(CustomReactionEvents.DismissCustomReactionSheet)
            onCustomEmojiSelected(target.event.eventId, emoji)

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
                onEmojiSelected = ::onEmojiSelectedDismiss,
                onCustomEmojiSelected = ::onCustomEmojiSelectedDismiss,
                emojibaseStore = target.emojibaseStore,
                selectedEmojis = state.selectedEmoji,
                recentEmojiDataSource = recentEmojiDataSource,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}
