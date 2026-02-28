package io.element.android.features.messages.impl.timeline.factories.event

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import chat.schildi.theme.ScTheme
import chat.schildi.theme.scBubbleFont
import com.beeper.android.messageformat.DefaultMatrixBodyStyledFormatter
import com.beeper.android.messageformat.MENTION_ROOM
import com.beeper.android.messageformat.MatrixBodyDrawStyle
import com.beeper.android.messageformat.MatrixBodyParseResult
import com.beeper.android.messageformat.MatrixBodyPreFormatStyle
import com.beeper.android.messageformat.MatrixBodyStyledFormatter
import com.beeper.android.messageformat.MatrixHtmlParser
import com.beeper.android.messageformat.MatrixToLink
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.timeline.item.event.MessageFormat
import io.element.android.libraries.matrix.api.timeline.item.event.MessageTypeWithAttachment
import io.element.android.libraries.matrix.api.timeline.item.event.TextLikeMessageType
import org.jsoup.nodes.Document

private const val ALLOW_PREPARSED = true

object MessageFormatDefaults {
    val blockIndention = 16.sp
    val parser: MatrixHtmlParser = MatrixHtmlParser()
    val parseStyle: MatrixBodyPreFormatStyle = MatrixBodyPreFormatStyle(
        formatRoomMention = {
            // Wrap in non-breakable space to add padding for background
            "\u00A0$MENTION_ROOM\u00A0"
        },
        formatUserMention = { _, content ->
            // Wrap in non-breakable space to add padding for background
            buildAnnotatedString {
                append("\u00A0")
                append(content)
                append("\u00A0")
            }
        },
    )
}

fun scFormattedBody(message: TextLikeMessageType, doc: Document?, isRoomMention: Boolean?, plaintextBody: String = message.body): MatrixBodyParseResult {
    return doc?.takeIf { ALLOW_PREPARSED }?.let {
        MessageFormatDefaults.parser.parseHtml(doc, MessageFormatDefaults.parseStyle, isRoomMention ?: true)
    } ?: message.formatted?.let { formatted ->
        formatted.body.takeIf { formatted.format == MessageFormat.HTML }?.let {
            MessageFormatDefaults.parser.parseHtml(it, MessageFormatDefaults.parseStyle, isRoomMention ?: true)
        }
    } ?: MessageFormatDefaults.parser.parsePlaintext(
        plaintextBody,
        MessageFormatDefaults.parseStyle,
        isRoomMention ?: true,
    )
}

fun scFormattedBody(message: MessageTypeWithAttachment, doc: Document?, isRoomMention: Boolean?): MatrixBodyParseResult? {
    return doc?.takeIf { ALLOW_PREPARSED }?.let {
        MessageFormatDefaults.parser.parseHtml(doc, MessageFormatDefaults.parseStyle, isRoomMention ?: true)
    } ?: message.formattedCaption?.let { formatted ->
        formatted.body.takeIf { formatted.format == MessageFormat.HTML }?.let {
            MessageFormatDefaults.parser.parseHtml(it, MessageFormatDefaults.parseStyle, isRoomMention ?: true)
        }
    } ?: message.caption?.let {
        MessageFormatDefaults.parser.parsePlaintext(it, MessageFormatDefaults.parseStyle, isRoomMention ?: true)
    }
}

fun scFormattedPlaintextBody(body: String, isRoomMention: Boolean?): MatrixBodyParseResult {
    return MessageFormatDefaults.parser.parsePlaintext(body, MessageFormatDefaults.parseStyle, isRoomMention ?: true)
}

val LocalSessionId = compositionLocalOf<SessionId?> { null }
val LocalMatrixBodyFormatter = compositionLocalOf<MatrixBodyStyledFormatter?> { null }
val LocalMatrixBodyDrawStyle = compositionLocalOf<MatrixBodyDrawStyle?> { null }

@Composable
fun matrixBodyFormatter(
    sessionId: SessionId? = LocalSessionId.current,
    onLinkClick: ((String) -> Unit)? = null,
    onMatrixLinkClick: ((MatrixToLink) -> Unit)? = null,
): MatrixBodyStyledFormatter {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val textStyle = ElementTheme.typography.scBubbleFont
    val linkColor = ElementTheme.colors.textLinkExternal
    val mentionColor = ScTheme.exposures.mentionFg
    val mentionHighlightColor = ScTheme.exposures.mentionFgHighlight
    val localUriHandler = LocalUriHandler.current
    val safeOnLinkClick = onLinkClick ?: localUriHandler::openUri
    return remember(
        density,
        textMeasurer,
        textStyle,
        linkColor,
        mentionColor,
        mentionHighlightColor,
        sessionId,
        safeOnLinkClick,
        onMatrixLinkClick,
    ) {
        fun clickableMatrixToLinkAnnotation(
            link: MatrixToLink,
            styles: TextLinkStyles? = TextLinkStyles(),
            interceptLinkClick: () -> Boolean,
        ): LinkAnnotation? {
            return onMatrixLinkClick?.let {
                LinkAnnotation.Clickable("mto_${link.rawUrl}", styles = styles) {
                    if (interceptLinkClick()) {
                        return@Clickable
                    }
                    onMatrixLinkClick(link)
                }
            } ?: LinkAnnotation.Clickable("mto_${link.rawUrl}", styles = styles) {
                if (interceptLinkClick()) {
                    return@Clickable
                }
                safeOnLinkClick(link.rawUrl)
            }
        }
        object : DefaultMatrixBodyStyledFormatter(
            density,
            textMeasurer,
            textStyle,
            urlStyle = TextLinkStyles(SpanStyle(color = linkColor)),
            blockIndention = MessageFormatDefaults.blockIndention,
            handleWebLinkClick = safeOnLinkClick,
        ) {
            override fun formatUserMention(mention: MatrixToLink.UserMention, context: FormatContext) = listOfNotNull(
                clickableMatrixToLinkAnnotation(mention) { interceptLinkClicks(context) },
                SpanStyle(
                    color = if (sessionId?.value == mention.userId) mentionHighlightColor else mentionColor,
                    fontWeight = FontWeight.Bold,
                ),
            )
            override fun formatRoomMention(context: FormatContext) = listOf(
                SpanStyle(color = mentionHighlightColor, fontWeight = FontWeight.Bold)
            )
            override fun formatRoomLink(roomLink: MatrixToLink.RoomLink, context: FormatContext) = listOfNotNull(
                clickableMatrixToLinkAnnotation(roomLink, styles = TextLinkStyles(style = SpanStyle(color = linkColor))) {
                    interceptLinkClicks(context)
                },
            )

            override fun formatMessageLink(messageLink: MatrixToLink.MessageLink, context: FormatContext) = listOfNotNull(
                clickableMatrixToLinkAnnotation(messageLink, styles = TextLinkStyles(style = SpanStyle(color = linkColor))) {
                    interceptLinkClicks(context)
                },
            )
        }
    }
}

@Composable
fun matrixBodyDrawStyle(sessionId: SessionId? = LocalSessionId.current): MatrixBodyDrawStyle {
    val mentionColor = ScTheme.exposures.mentionBg
    val mentionHighlightColor = ScTheme.exposures.mentionBgHighlight
    val onSurface = ElementTheme.colors.textSecondary
    val onSurfaceVariant = ScTheme.exposures.tertiaryFgNoAlpha
    return remember(
        mentionColor,
        mentionHighlightColor,
        onSurface,
        onSurfaceVariant,
        sessionId,
    ) {
        MatrixBodyDrawStyle(
            defaultForegroundColor = onSurfaceVariant,
            drawBehindRoomMention = { position ->
                drawRoundRect(
                    mentionHighlightColor,
                    topLeft = position.rect.topLeft,
                    size = position.rect.size,
                    cornerRadius = mentionBgRadius(),
                )
            },
            drawBehindUserMention = { mention, position ->
                val color = if (sessionId?.value == mention.userId) {
                    mentionHighlightColor
                } else {
                    mentionColor
                }
                drawRoundRect(
                    color,
                    topLeft = position.rect.topLeft,
                    size = position.rect.size,
                    cornerRadius = mentionBgRadius(),
                )
            },
            drawBehindBlockQuote = { depth, position ->
                val barWidthDp = 4f
                drawRoundRect(
                    onSurfaceVariant,
                    topLeft = Offset((MessageFormatDefaults.blockIndention * (depth - 1)).toPx(), position.rect.top),
                    size = Size(barWidthDp * density, position.rect.height),
                    cornerRadius = CornerRadius(barWidthDp * density, barWidthDp * density),
                )
            },
            drawBehindDetailsSummaryFirstLine = { revealId, pos, state ->
                val rect = pos.rect
                // Use line height and available width as baseline size for triangle size
                val lineHeight = rect.size.height
                val triangleSideLength = lineHeight / 2f
                // * sqrt(3) / 2
                val triangleHeight = triangleSideLength * 0.8660254f
                val shortSidePadding = (triangleSideLength - triangleHeight) / 2
                val trianglePath = Path().apply {
                    if (revealId in state.expandedItems.value) {
                        // Already expanded => downward-facing triangle
                        moveTo(0f, shortSidePadding)
                        lineTo(triangleSideLength / 2, triangleSideLength - shortSidePadding)
                        lineTo(triangleSideLength, shortSidePadding)
                    } else {
                        moveTo(shortSidePadding, 0f)
                        lineTo(triangleSideLength - shortSidePadding, triangleSideLength / 2)
                        lineTo(shortSidePadding, triangleSideLength)
                    }
                    close()
                    val canvasPadding = (lineHeight - triangleSideLength) / 2
                    translate(
                        Offset(
                            if (pos.isRtl) {
                                rect.right - triangleSideLength + canvasPadding
                            } else {
                                rect.left
                            },
                            // Center in line height
                            rect.top + canvasPadding,
                        )
                    )
                }
                drawPath(trianglePath, onSurface)
            },
        )
    }
}

private fun DrawScope.mentionBgRadius(): CornerRadius {
    val radius = 8f * density
    return CornerRadius(radius, radius)
}

