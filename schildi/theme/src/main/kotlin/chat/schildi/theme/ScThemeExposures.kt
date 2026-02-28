package chat.schildi.theme

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.compound.annotations.CoreColorToken
import io.element.android.compound.tokens.generated.internal.DarkColorTokens
import io.element.android.compound.tokens.generated.internal.LightColorTokens

/**
  * This class collects all additional SC values that we want to add to Element's SemanticColors.
  */
@Stable
class ScThemeExposures(
    isScTheme: Boolean,
    horizontalDividerThickness: Dp,
    colorOnAccent: Color,
    bubbleBgIncoming: Color?,
    bubbleBgOutgoing: Color?,
    unreadBadgeColor: Color,
    unreadBadgeOnToolbarColor: Color,
    appBarBg: Color?,
    bubbleRadius: Dp,
    timestampRadius: Dp,
    commonLayoutRadius: Dp,
    timestampOverlayBg: Color,
    unreadIndicatorLine: Color?,
    unreadIndicatorThickness: Dp,
    mentionFgLegacy: Color?,
    mentionBgLegacy: Color?,
    mentionBgOtherLegacy: Color?,
    mentionFg: Color,
    mentionBg: Color,
    mentionFgHighlight: Color,
    mentionBgHighlight: Color,
    greenFg: Color?,
    greenBg: Color?,
    messageHighlightBg: Color?,
    composerBlockBg: Color?,
    composerBlockFg: Color?,
    spaceBarBg: Color?,
    tertiaryFgNoAlpha: Color,
) {
    var isScTheme by mutableStateOf(isScTheme)
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
    var unreadBadgeOnToolbarColor by mutableStateOf(unreadBadgeOnToolbarColor)
        private set
    var appBarBg by mutableStateOf(appBarBg)
        private set
    var bubbleRadius by mutableStateOf(bubbleRadius)
        private set
    var commonLayoutRadius by mutableStateOf(commonLayoutRadius)
        private set
    var timestampRadius by mutableStateOf(timestampRadius)
        private set
    var timestampOverlayBg by mutableStateOf(timestampOverlayBg)
        private set
    var unreadIndicatorLine by mutableStateOf(unreadIndicatorLine)
        private set
    var unreadIndicatorThickness by mutableStateOf(unreadIndicatorThickness)
        private set
    var mentionFgLegacy by mutableStateOf(mentionFgLegacy)
        private set
    var mentionBgLegacy by mutableStateOf(mentionBgLegacy)
        private set
    var mentionBgOtherLegacy by mutableStateOf(mentionBgOtherLegacy)
        private set
    var mentionFg by mutableStateOf(mentionFg)
        private set
    var mentionBg by mutableStateOf(mentionBg)
        private set
    var mentionFgHighlight by mutableStateOf(mentionFgHighlight)
        private set
    var mentionBgHighlight by mutableStateOf(mentionBgHighlight)
        private set
    var greenFg by mutableStateOf(greenFg)
        private set
    var greenBg by mutableStateOf(greenBg)
        private set
    var messageHighlightBg by mutableStateOf(messageHighlightBg)
        private set
    var composerBlockBg by mutableStateOf(composerBlockBg)
        private set
    var composerBlockFg by mutableStateOf(composerBlockFg)
        private set
    var spaceBarBg by mutableStateOf(spaceBarBg)
        private set
    var tertiaryFgNoAlpha by mutableStateOf(tertiaryFgNoAlpha)
        private set

    fun copy(
        isScTheme: Boolean = this.isScTheme,
        horizontalDividerThickness: Dp = this.horizontalDividerThickness,
        colorOnAccent: Color = this.colorOnAccent,
        bubbleBgIncoming: Color? = this.bubbleBgIncoming,
        bubbleBgOutgoing: Color? = this.bubbleBgOutgoing,
        unreadBadgeColor: Color = this.unreadBadgeColor,
        unreadBadgeOnToolbarColor: Color = this.unreadBadgeOnToolbarColor,
        appBarBg: Color? = this.appBarBg,
        bubbleRadius: Dp = this.bubbleRadius,
        commonLayoutRadius: Dp = this.commonLayoutRadius,
        timestampRadius: Dp = this.timestampRadius,
        timestampOverlayBg: Color = this.timestampOverlayBg,
        unreadIndicatorLine: Color? = this.unreadIndicatorLine,
        unreadIndicatorThickness: Dp = this.unreadIndicatorThickness,
        mentionFgLegacy: Color? = this.mentionFgLegacy,
        mentionBgLegacy: Color? = this.mentionBgLegacy,
        mentionBgOtherLegacy: Color? = this.mentionBgOtherLegacy,
        mentionFg: Color = this.mentionFg,
        mentionBg: Color = this.mentionBg,
        mentionFgHighlight: Color = this.mentionFgHighlight,
        mentionBgHighlight: Color = this.mentionBgHighlight,
        greenFg: Color? = this.greenFg,
        greenBg: Color? = this.greenBg,
        messageHighlightBg: Color? = this.messageHighlightBg,
        composerBlockBg: Color? = this.composerBlockBg,
        composerBlockFg: Color? = this.composerBlockFg,
        spaceBarBg: Color? = this.spaceBarBg,
        tertiaryFgNoAlpha: Color = this.tertiaryFgNoAlpha,
    ) = ScThemeExposures(
        isScTheme = isScTheme,
        horizontalDividerThickness = horizontalDividerThickness,
        colorOnAccent = colorOnAccent,
        bubbleBgIncoming = bubbleBgIncoming,
        bubbleBgOutgoing = bubbleBgOutgoing,
        unreadBadgeColor = unreadBadgeColor,
        unreadBadgeOnToolbarColor = unreadBadgeOnToolbarColor,
        appBarBg = appBarBg,
        bubbleRadius = bubbleRadius,
        commonLayoutRadius = commonLayoutRadius,
        timestampRadius = timestampRadius,
        timestampOverlayBg = timestampOverlayBg,
        unreadIndicatorLine = unreadIndicatorLine,
        unreadIndicatorThickness = unreadIndicatorThickness,
        mentionFgLegacy = mentionFgLegacy,
        mentionBgLegacy = mentionBgLegacy,
        mentionBgOtherLegacy = mentionBgOtherLegacy,
        mentionFg = mentionFg,
        mentionBg = mentionBg,
        mentionFgHighlight = mentionFgHighlight,
        mentionBgHighlight = mentionBgHighlight,
        greenFg = greenFg,
        greenBg = greenBg,
        messageHighlightBg = messageHighlightBg,
        composerBlockBg = composerBlockBg,
        composerBlockFg = composerBlockFg,
        spaceBarBg = spaceBarBg,
        tertiaryFgNoAlpha = tertiaryFgNoAlpha,
    )

    fun updateColorsFrom(other: ScThemeExposures) {
        isScTheme = other.isScTheme
        horizontalDividerThickness = other.horizontalDividerThickness
        colorOnAccent = other.colorOnAccent
        bubbleBgIncoming = other.bubbleBgIncoming
        bubbleBgOutgoing = other.bubbleBgOutgoing
        unreadBadgeColor = other.unreadBadgeColor
        unreadBadgeOnToolbarColor = other.unreadBadgeOnToolbarColor
        appBarBg = other.appBarBg
        bubbleRadius = other.bubbleRadius
        commonLayoutRadius = other.commonLayoutRadius
        timestampRadius = other.timestampRadius
        timestampOverlayBg = other.timestampOverlayBg
        unreadIndicatorLine = other.unreadIndicatorLine
        unreadIndicatorThickness = other.unreadIndicatorThickness
        mentionFgLegacy = other.mentionFgLegacy
        mentionBgLegacy = other.mentionBgLegacy
        mentionBgOtherLegacy = other.mentionBgOtherLegacy
        mentionFg = other.mentionFg
        mentionBg = other.mentionBg
        mentionFgHighlight = other.mentionFgHighlight
        mentionBgHighlight = other.mentionBgHighlight
        greenFg = other.greenFg
        greenBg = other.greenBg
        messageHighlightBg = other.messageHighlightBg
        composerBlockBg = other.composerBlockBg
        composerBlockFg = other.composerBlockFg
        spaceBarBg = other.spaceBarBg
        tertiaryFgNoAlpha = other.tertiaryFgNoAlpha
    }
}

@OptIn(CoreColorToken::class)
internal val elementLightScExposures = ScThemeExposures(
    isScTheme = false,
    horizontalDividerThickness = 0.5.dp, /** [io.element.android.libraries.designsystem.theme.components.HorizontalDivider] */
    colorOnAccent = Color.White,
    bubbleBgIncoming = null,
    bubbleBgOutgoing = null,
    unreadBadgeColor = Color(0xffa9b2bc),
    unreadBadgeOnToolbarColor = Color(0xffa9b2bc),
    appBarBg = null,
    bubbleRadius = 12.dp,
    commonLayoutRadius = 10.dp,
    timestampRadius = 10.0.dp,
    timestampOverlayBg = Color.Magenta, // unused for non-SC themes
    unreadIndicatorLine = null,
    unreadIndicatorThickness = 0.5.dp, // like horizontalDividerThickness
    mentionFgLegacy = null,
    mentionBgLegacy = null,
    mentionBgOtherLegacy = null,
    mentionFg = LightColorTokens.colorGray1400,
    mentionBg = LightColorTokens.colorAlphaGray400,
    mentionBgHighlight = LightColorTokens.colorAlphaGreen400,
    mentionFgHighlight = DarkColorTokens.colorGreen1100,
    greenFg = null,
    greenBg = null,
    messageHighlightBg = null,
    composerBlockBg = null,
    composerBlockFg = null,
    spaceBarBg = null,
    tertiaryFgNoAlpha = LightColorTokens.colorGray1100,
)

@OptIn(CoreColorToken::class)
internal val elementDarkScExposures = ScThemeExposures(
    isScTheme = false,
    horizontalDividerThickness = 0.5.dp, /** [io.element.android.libraries.designsystem.theme.components.HorizontalDivider] */
    colorOnAccent = Color.White,
    bubbleBgIncoming = null,
    bubbleBgOutgoing = null,
    unreadBadgeColor = Color(0xff737d8c),
    unreadBadgeOnToolbarColor = Color(0xff737d8c),
    appBarBg = null,
    bubbleRadius = 12.dp,
    commonLayoutRadius = 10.dp,
    timestampRadius = 10.0.dp,
    timestampOverlayBg = Color.Magenta, // unused for non-SC themes
    unreadIndicatorLine = null,
    unreadIndicatorThickness = 0.5.dp, // like horizontalDividerThickness
    mentionFgLegacy = null,
    mentionBgLegacy = null,
    mentionBgOtherLegacy = null,
    mentionFg = DarkColorTokens.colorGray1400,
    mentionBg = DarkColorTokens.colorAlphaGray500,
    mentionBgHighlight = DarkColorTokens.colorAlphaGreen500,
    mentionFgHighlight = DarkColorTokens.colorGreen1100,
    greenFg = null,
    greenBg = null,
    messageHighlightBg = null,
    composerBlockBg = null,
    composerBlockFg = null,
    spaceBarBg = null,
    tertiaryFgNoAlpha = DarkColorTokens.colorGray1100,
)
