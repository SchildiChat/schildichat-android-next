package io.element.android.libraries.matrix.ui.messages

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Document.OutputSettings
import org.jsoup.nodes.TextNode
import org.jsoup.safety.Safelist

internal fun fixInlineImages(
    dom: Document,
) {
    val images = dom.getElementsByTag("img")
    images.forEach {
        val fallback = it.attr("alt").takeIf(String::isNotBlank) ?: it.attr("title").takeIf(String::isNotBlank) ?: return@forEach
        it.replaceWith(TextNode(fallback))
    }
}

/**
 * Original from io.element.android.wysiwyg.utils,
 * but too annoying to have to rebuild the whole Rust library for patching that
 */
object ScHtmlToDomParser {
    fun document(html: String): Document {
        val outputSettings = OutputSettings().prettyPrint(false).indentAmount(0)
        val cleanHtml = Jsoup.clean(html, "", safeList, outputSettings)
        return Jsoup.parse(cleanHtml)
    }

    private val safeList = Safelist()
        .addTags(
            "a", "b", "strong", "i", "em", "u", "del", "code", "ul", "ol", "li", "pre",
            "blockquote", "p", "br", "img", "span", "h1", "h2", "h3", "h4", "h5", "h6", "s", "font",
            "div", "hr", "details", "summary",
        )
        .addAttributes("a", "href", "data-mention-type", "contenteditable")
        .addAttributes("img", "src", "title", "alt", "width", "height", "data-mx-emoticon")
        .addAttributes("ol", "start")
        .addAttributes("li", "value")
        .addAttributes("span", "data-mx-color", "color", "data-mx-bg-color", "data-mx-spoiler")
        .addAttributes("font", "data-mx-color", "color", "data-mx-bg-color", "data-mx-spoiler")
}
