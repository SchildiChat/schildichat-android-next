package io.element.android.libraries.matrix.ui.messages

import org.jsoup.nodes.Document
import org.jsoup.nodes.TextNode

internal fun fixInlineImages(
    dom: Document,
) {
    val images = dom.getElementsByTag("img")
    images.forEach {
        val fallback = it.attr("alt").takeIf(String::isNotBlank) ?: it.attr("title").takeIf(String::isNotBlank) ?: return@forEach
        it.replaceWith(TextNode(fallback))
    }
}
