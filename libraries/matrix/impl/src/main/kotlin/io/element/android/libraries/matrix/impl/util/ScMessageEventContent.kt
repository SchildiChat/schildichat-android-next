package io.element.android.libraries.matrix.impl.util

import io.element.android.libraries.matrix.api.room.IntentionalMention
import io.element.android.libraries.matrix.impl.room.map
import org.matrix.rustcomponents.sdk.RoomMessageEventContentWithoutRelation
import org.matrix.rustcomponents.sdk.messageEventContentFromHtml
import org.matrix.rustcomponents.sdk.messageEventContentFromHtmlAsEmote
import org.matrix.rustcomponents.sdk.messageEventContentFromHtmlAsNotice
import org.matrix.rustcomponents.sdk.messageEventContentFromMarkdown
import org.matrix.rustcomponents.sdk.messageEventContentFromMarkdownAsEmote
import org.matrix.rustcomponents.sdk.messageEventContentFromMarkdownAsNotice
import org.matrix.rustcomponents.sdk.messageEventContentFromPlaintext
import org.matrix.rustcomponents.sdk.messageEventContentFromPlaintextAsEmote
import org.matrix.rustcomponents.sdk.messageEventContentFromPlaintextAsNotice

/**
 * Creates a [RoomMessageEventContentWithoutRelation] from a body, an html body and a list of mentions.
 */
object ScMessageEventContent {
    fun from(
        body: String,
        htmlBody: String?,
        plaintext: Boolean,
        intentionalMentions: List<IntentionalMention>,
    ): RoomMessageEventContentWithoutRelation {
        return when {
            htmlBody != null -> messageEventContentFromHtml(body, htmlBody)
            plaintext -> messageEventContentFromPlaintext(body)
            else -> messageEventContentFromMarkdown(body)
        }.withMentions(intentionalMentions.map())
    }
}

object NoticeEventContent {
    fun from(
        body: String,
        htmlBody: String?,
        plaintext: Boolean,
        intentionalMentions: List<IntentionalMention>,
    ): RoomMessageEventContentWithoutRelation {
        return when {
            htmlBody != null -> messageEventContentFromHtmlAsNotice(body, htmlBody)
            plaintext -> messageEventContentFromPlaintextAsNotice(body)
            else -> messageEventContentFromMarkdownAsNotice(body)
        }.withMentions(intentionalMentions.map())
    }
}

object EmoteEventContent {
    fun from(
        body: String,
        htmlBody: String?,
        plaintext: Boolean,
        intentionalMentions: List<IntentionalMention>,
    ): RoomMessageEventContentWithoutRelation {
        return when {
            htmlBody != null -> messageEventContentFromHtmlAsEmote(body, htmlBody)
            plaintext -> messageEventContentFromPlaintextAsEmote(body)
            else -> messageEventContentFromMarkdownAsEmote(body)
        }.withMentions(intentionalMentions.map())
    }
}
