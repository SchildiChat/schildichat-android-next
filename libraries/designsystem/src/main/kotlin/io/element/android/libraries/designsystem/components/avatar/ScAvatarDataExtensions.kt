package io.element.android.libraries.designsystem.components.avatar

import io.element.android.libraries.core.data.tryOrNull
import java.text.BreakIterator

fun AvatarData.scPreviewLetters(): String? {
    // For roomIds, use "#" as initial
    return name?.takeIf { it.isNotBlank() }
        ?.let { dn ->
            val (firstChar, index) = dn.upstreamAlgNullable(dn) ?: return null
            var rest = dn.substring(index)
            val firstSeparator = rest.indexOfFirst { !it.isLetterOrDigit() }.takeIf { it >= 0 } ?: return firstChar
            rest = rest.substring(firstSeparator)
            val secondCharIndex = rest.indexOfFirst { it.isLetterOrDigit() }.takeIf { it >= 0 } ?: return firstChar
            val (secondChar, _) = dn.upstreamAlgNullable(rest, secondCharIndex) ?: return firstChar
            return "$firstChar$secondChar"
        }
}

private fun String.upstreamAlgNullable(dn: String, initialStartIndex: Int = 0): Pair<String, Int>? {
    var startIndex = initialStartIndex
    val initial = dn[startIndex]

    if (initial in listOf('@', '#', '+') && dn.length > initialStartIndex + 1) {
        startIndex++
    }

    var next = dn[startIndex]

    // LEFT-TO-RIGHT MARK
    if (dn.length >= 2 && 0x200e == next.code) {
        startIndex++
        next = dn[startIndex]
    }

    while (next.isWhitespace()) {
        if (dn.length > startIndex + 1) {
            startIndex++
            next = dn[startIndex]
        } else {
            break
        }
    }

    val fullCharacterIterator = BreakIterator.getCharacterInstance()
    fullCharacterIterator.setText(dn)
    val glyphBoundary = tryOrNull { fullCharacterIterator.following(startIndex) }
        ?.takeIf { it in startIndex..dn.length }

    return when {
        // Use the found boundary
        glyphBoundary != null -> Pair(dn.substring(startIndex, glyphBoundary), glyphBoundary)
        // If no boundary was found, default to the next char if possible
        startIndex + 1 < dn.length -> Pair(dn.substring(startIndex, startIndex + 1), startIndex + 1)
        else -> null
    }
}
