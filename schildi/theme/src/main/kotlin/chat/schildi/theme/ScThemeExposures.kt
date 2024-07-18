package chat.schildi.theme

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
  * This class collects all additional SC values that we want to add to Element's SemanticColors.
  */
@Stable
class ScThemeExposures(
    isScTheme: Boolean,
    isDynamicColor: Boolean,
    horizontalDividerThickness: Dp,
    colorOnAccent: Color,
    bubbleBgIncoming: Color?,
    bubbleBgOutgoing: Color?,
    unreadBadgeColor: Color,
    appBarBg: Color?,
    bubbleRadius: Dp,
    timestampRadius: Dp,
    timestampOverlayBg: Color,
    unreadIndicatorLine: Color?,
    unreadIndicatorThickness: Dp,
    mentionFg: Color?,
    mentionBg: Color?,
    mentionBgOther: Color?,
    greenFg: Color?,
    greenBg: Color?,
    messageHighlightBg: Color?,
) {
    var isScTheme by mutableStateOf(isScTheme)
        private set
    var isDynamicColor by mutableStateOf(isDynamicColor)
        private set
    var horizontalDividerThickness by mutableStateOf(horizontalDividerThickness)
        private set
    var colorOnAccent by mutableStateOf(colorOnAccent)
        private set
    var bubbleBgIncoming by mutableStateOf(bubbleBgIncoming)
        private set
    var bubbleBgOutgoing by mutableStateOf(bubbleBgOutgoing)
        private set
    var unreadBadgeColor by mutableStateOf(unreadBadgeColor)
        private set
    var appBarBg by mutableStateOf(appBarBg)
        private set
    var bubbleRadius by mutableStateOf(bubbleRadius)
        private set
    var timestampRadius by mutableStateOf(timestampRadius)
        private set
    var timestampOverlayBg by mutableStateOf(timestampOverlayBg)
        private set
    var unreadIndicatorLine by mutableStateOf(unreadIndicatorLine)
        private set
    var unreadIndicatorThickness by mutableStateOf(unreadIndicatorThickness)
        private set
    var mentionFg by mutableStateOf(mentionFg)
        private set
    var mentionBg by mutableStateOf(mentionBg)
        private set
    var mentionBgOther by mutableStateOf(mentionBgOther)
        private set
    var greenFg by mutableStateOf(greenFg)
        private set
    var greenBg by mutableStateOf(greenBg)
        private set
    var messageHighlightBg by mutableStateOf(greenBg)
        private set

    fun copy(
        isScTheme: Boolean = this.isScTheme,
        isDynamicColor: Boolean = this.isDynamicColor,
        horizontalDividerThickness: Dp = this.horizontalDividerThickness,
        colorOnAccent: Color = this.colorOnAccent,
        bubbleBgIncoming: Color? = this.bubbleBgIncoming,
        bubbleBgOutgoing: Color? = this.bubbleBgOutgoing,
        unreadBadgeColor: Color = this.unreadBadgeColor,
        appBarBg: Color? = this.appBarBg,
        bubbleRadius: Dp = this.bubbleRadius,
        timestampRadius: Dp = this.timestampRadius,
        timestampOverlayBg: Color = this.timestampOverlayBg,
        unreadIndicatorLine: Color? = this.unreadIndicatorLine,
        unreadIndicatorThickness: Dp = this.unreadIndicatorThickness,
        mentionFg: Color? = this.mentionFg,
        mentionBg: Color? = this.mentionBg,
        mentionBgOther: Color? = this.mentionBgOther,
        greenFg: Color? = this.greenFg,
        greenBg: Color? = this.greenBg,
        messageHighlightBg: Color? = this.messageHighlightBg,
    ) = ScThemeExposures(
        isScTheme = isScTheme,
        isDynamicColor = isDynamicColor,
        horizontalDividerThickness = horizontalDividerThickness,
        colorOnAccent = colorOnAccent,
        bubbleBgIncoming = bubbleBgIncoming,
        bubbleBgOutgoing = bubbleBgOutgoing,
        unreadBadgeColor = unreadBadgeColor,
        appBarBg = appBarBg,
        bubbleRadius = bubbleRadius,
        timestampRadius = timestampRadius,
        timestampOverlayBg = timestampOverlayBg,
        unreadIndicatorLine = unreadIndicatorLine,
        unreadIndicatorThickness = unreadIndicatorThickness,
        mentionFg = mentionFg,
        mentionBg = mentionBg,
        mentionBgOther = mentionBgOther,
        greenFg = greenFg,
        greenBg = greenBg,
        messageHighlightBg = messageHighlightBg,
    )

    fun updateColorsFrom(other: ScThemeExposures) {
        isScTheme = other.isScTheme
        isDynamicColor = other.isDynamicColor
        horizontalDividerThickness = other.horizontalDividerThickness
        colorOnAccent = other.colorOnAccent
        bubbleBgIncoming = other.bubbleBgIncoming
        bubbleBgOutgoing = other.bubbleBgOutgoing
        unreadBadgeColor = other.unreadBadgeColor
        appBarBg = other.appBarBg
        bubbleRadius = other.bubbleRadius
        timestampRadius = other.timestampRadius
        timestampOverlayBg = other.timestampOverlayBg
        unreadIndicatorLine = other.unreadIndicatorLine
        unreadIndicatorThickness = other.unreadIndicatorThickness
        mentionFg = other.mentionFg
        mentionBg = other.mentionBg
        mentionBgOther = other.mentionBgOther
        greenFg = other.greenFg
        greenBg = other.greenBg
        messageHighlightBg = other.messageHighlightBg
    }
}

internal val elementLightScExposures = ScThemeExposures(
    isScTheme = false,
    isDynamicColor = false,
    horizontalDividerThickness = 0.5.dp, /** [io.element.android.libraries.designsystem.theme.components.HorizontalDivider] */
    colorOnAccent = Color.White,
    bubbleBgIncoming = null,
    bubbleBgOutgoing = null,
    unreadBadgeColor = Color(0xffa9b2bc),
    appBarBg = null,
    bubbleRadius = 12.dp,
    timestampRadius = 10.0.dp,
    timestampOverlayBg = Color.Magenta, // unused for non-SC themes
    unreadIndicatorLine = null,
    unreadIndicatorThickness = 0.5.dp, // like horizontalDividerThickness
    mentionFg = null,
    mentionBg = null,
    mentionBgOther = null,
    greenFg = null,
    greenBg = null,
    messageHighlightBg = null,
)

internal val elementDarkScExposures = ScThemeExposures(
    isScTheme = false,
    isDynamicColor = false,
    horizontalDividerThickness = 0.5.dp, /** [io.element.android.libraries.designsystem.theme.components.HorizontalDivider] */
    colorOnAccent = Color.White,
    bubbleBgIncoming = null,
    bubbleBgOutgoing = null,
    unreadBadgeColor = Color(0xff737d8c),
    appBarBg = null,
    bubbleRadius = 12.dp,
    timestampRadius = 10.0.dp,
    timestampOverlayBg = Color.Magenta, // unused for non-SC themes
    unreadIndicatorLine = null,
    unreadIndicatorThickness = 0.5.dp, // like horizontalDividerThickness
    mentionFg = null,
    mentionBg = null,
    mentionBgOther = null,
    greenFg = null,
    greenBg = null,
    messageHighlightBg = null,
)
