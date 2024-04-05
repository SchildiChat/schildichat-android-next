package io.element.android.features.messages.impl.timeline.factories.event

import io.element.android.libraries.matrix.api.timeline.item.event.FormattedBody
import io.element.android.libraries.matrix.api.timeline.item.event.MessageFormat
import io.element.android.libraries.matrix.ui.messages.toHtmlDocument
import timber.log.Timber

internal fun TimelineItemContentMessageFactory.parseHtmlCollapsed(formattedBody: FormattedBody?, prefix: String? = null): CharSequence? {
    if (formattedBody == null || formattedBody.format != MessageFormat.HTML) return null
    // If nothing collapsible, no need to do anything
    if ("<details" !in formattedBody.body) {
        return null
    }
    val html = formattedBody.toHtmlDocument(prefix)?.body() ?: return null
    html.getElementsByTag("details").forEach { details ->
        val summaries = details.getElementsByTag("summary")
        if (summaries.size != 1) {
            Timber.w("Found details tag with ${summaries.size} summaries, ignoring")
            return@forEach
        }
        val summary = summaries[0]
        details.before("&#9654; ")
        details.replaceWith(
            summary
        )
    }
    return parseHtml(formattedBody.copy(body = html.html()), prefix)
}

internal fun String.escapeHtmlBeforeWysiwyg(): String {
    return replace("<br>\n", "<br>")
        .replace(Regex("""<a href="(https://matrix.to/#/[^"]*")></a>""")) {
            """<a href="${it.groupValues[1]}">UNKNOWN</a>"""
        }
}
