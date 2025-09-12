package io.element.android.features.messages.impl.emojis

import chat.schildi.matrixsdk.ACCOUNT_DATA_RECENT_EMOJI
import chat.schildi.matrixsdk.RecentEmojiItem
import chat.schildi.matrixsdk.RecentEmojiSerializer
import chat.schildi.matrixsdk.isValidRecentEmoji
import chat.schildi.matrixsdk.recordSelection
import dev.zacsweers.metro.Inject
import io.element.android.libraries.matrix.api.MatrixClient
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

@Inject
class RecentEmojiDataSource(
    private val client: MatrixClient,
) {

    private suspend fun getCurrentRecentEmojis(): List<RecentEmojiItem> {
        val result = client.getAccountData(ACCOUNT_DATA_RECENT_EMOJI)?.let { RecentEmojiSerializer.deserializeContent(it) }
        if (result?.isFailure != false) {
            Timber.w("Failed to load recent emojis: ${result?.exceptionOrNull()}")
            return emptyList()
        }
        return result.getOrNull().orEmpty()
    }

    suspend fun getRecentEmojisSorted(): List<String> {
        return getCurrentRecentEmojis().filter { isValidRecentEmoji(it.emoji) }.sortedByDescending { it.recurrences ?: 1L }.map { it.emoji }
    }

    suspend fun recordEmoji(emoji: String) {
        if (!isValidRecentEmoji(emoji)) {
            return
        }
        val data = getCurrentRecentEmojis().toMutableList()
        data.recordSelection(emoji)
        client.setAccountData(ACCOUNT_DATA_RECENT_EMOJI, RecentEmojiSerializer.serializeContent(data))
    }

    private val _recentEmojis = MutableStateFlow<ImmutableList<String>>(persistentListOf())
    val recentEmojis: StateFlow<ImmutableList<String>> = _recentEmojis

    fun refresh(coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            _recentEmojis.emit(getRecentEmojisSorted().toImmutableList())
        }
    }
}
