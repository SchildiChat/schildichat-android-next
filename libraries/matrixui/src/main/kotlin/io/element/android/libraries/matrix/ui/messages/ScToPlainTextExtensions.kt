package io.element.android.libraries.matrix.ui.messages

import org.jsoup.nodes.Document
import org.jsoup.nodes.TextNode

fun Document.stripSpoilers(): Document {
    select("span[data-mx-spoiler]").forEach { span ->
        span.replaceWith(TextNode("■".repeat(span.text().length.coerceIn(0, 5))))
    }
    return this
}
