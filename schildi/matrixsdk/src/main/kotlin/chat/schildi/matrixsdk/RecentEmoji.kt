package chat.schildi.matrixsdk

import androidx.emoji2.text.EmojiCompat
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.longOrNull
import timber.log.Timber
import java.lang.Character.UnicodeBlock.MISCELLANEOUS_SYMBOLS
import java.lang.Character.UnicodeBlock.MISCELLANEOUS_TECHNICAL
import java.lang.Character.UnicodeBlock.VARIATION_SELECTORS
import java.lang.Character.codePointAt
import java.lang.Character.codePointBefore
import java.lang.Character.isSupplementaryCodePoint
import java.lang.Character.isValidCodePoint

const val ACCOUNT_DATA_RECENT_EMOJI = "io.element.recent_emoji"

// Element web also limits to 100
private const val RECENT_EMOJI_LIMIT = 100

object RecentEmojiSerializer {
    private val coder = Json { ignoreUnknownKeys = true }
    fun deserializeContent(data: String): Result<List<RecentEmojiItem>> {
        return runCatching { coder.decodeFromString(RecentEmojiContent.serializer(), data).toRecentEmojiItems() }
    }
    fun serializeContent(data: List<RecentEmojiItem>): String {
        return coder.encodeToString(RecentEmojiContent.serializer(), data.toRecentEmojiRaw())
    }
}

data class RecentEmojiItem(
    val emoji: String,
    val recurrences: Long?,
)

@Serializable
data class RecentEmojiContent(
    @SerialName("recent_emoji")
    val recentEmoji: List<List<JsonPrimitive>>,
) {

    fun toRecentEmojiItems(): List<RecentEmojiItem> {
        return recentEmoji.mapNotNull {
            try {
                RecentEmojiItem(it[0].contentOrNull!!, it.getOrNull(1)?.longOrNull)
            } catch (t: Throwable) {
                null
            }
        }
    }
}

private fun List<RecentEmojiItem>.toRecentEmojiRaw(): RecentEmojiContent {
    return RecentEmojiContent(map {
        listOf(JsonPrimitive(it.emoji)) + if (it.recurrences != null) listOf(JsonPrimitive(it.recurrences)) else emptyList()
    })
}

public fun MutableList<RecentEmojiItem>.recordSelection(emoji: String) {
    // Only allow "pure" emojis, no other chars or multi-emojis
    if (!isValidRecentEmoji(emoji)) {
        return
    }
    val existingEntry = find { it.emoji == emoji }
    val newEntry = if (existingEntry == null) {
        RecentEmojiItem(emoji, 1L)
    } else {
        remove(existingEntry)
        existingEntry.copy(recurrences = (existingEntry.recurrences ?: 1) + 1)
    }
    add(0, newEntry)
    while (size > RECENT_EMOJI_LIMIT) {
        removeLast()
    }
}

fun isValidRecentEmoji(s: String): Boolean {
    try {
        return s.containsOnlyEmojis(1)
    } catch (t: Throwable) {
        Timber.d("EmojiCompat didn't work, Fall back to poor man's valid emoji detection")
        return poorMansIsValidRecentEmoji(s)
    }
}

// From https://stackoverflow.com/a/74800286
fun poorMansIsValidRecentEmoji(someString: String): Boolean {
    if (someString.isNotEmpty() && someString.length < 5) {
        val firstCodePoint = codePointAt(someString, 0)
        val lastCodePoint = codePointBefore(someString, someString.length)
        if (isValidCodePoint(firstCodePoint) && isValidCodePoint(lastCodePoint)) {
            if (isSupplementaryCodePoint(firstCodePoint) ||
                isSupplementaryCodePoint(lastCodePoint) ||
                Character.UnicodeBlock.of(firstCodePoint) == MISCELLANEOUS_SYMBOLS ||
                Character.UnicodeBlock.of(firstCodePoint) == MISCELLANEOUS_TECHNICAL ||
                Character.UnicodeBlock.of(lastCodePoint) == VARIATION_SELECTORS
            ) {
                return true
            }
        }
    }
    return false
}

fun String.containsOnlyEmojis(maxEmojis: Int = Integer.MAX_VALUE, throwOnError: Boolean = false): Boolean {
    if (maxEmojis <= 0) return false
    val emojiCompat = try {
        EmojiCompat.get()
    } catch (e: IllegalStateException) {
        Timber.e("EmojiCompat not initialized yet, cannot check for emoji-only-messages")
        if (throwOnError) throw e
        return false
    }
    if (emojiCompat.loadState != EmojiCompat.LOAD_STATE_SUCCEEDED) {
        Timber.e("EmojiCompat loadState: ${emojiCompat.loadState}, cannot check for emoji-only-messages")
        if (throwOnError) throw IllegalStateException("Unexpected EmojiCompat loadState ${emojiCompat.loadState}")
        return false
    }
    val start = emojiCompat.getEmojiStart(this, 0)
    if (start != 0) {
        return false
    }
    val end = emojiCompat.getEmojiEnd(this, 0)

    return if (end == -1) {
        false
    } else {
        if (end == length) {
            true
        } else {
            if (end < length) {
                substring(end).containsOnlyEmojis(maxEmojis = maxEmojis-1)
            } else {
                // error, return false for safety
                Timber.e("EmojiCompat returned unexpected end index")
                if (throwOnError) throw IllegalStateException("EmojiCompat returned unexpected end index")
                false
            }
        }
    }
}
