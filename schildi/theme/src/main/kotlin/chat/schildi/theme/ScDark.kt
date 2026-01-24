package chat.schildi.theme

import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.unit.dp
import io.element.android.compound.annotations.CoreColorToken
import io.element.android.compound.tokens.generated.SemanticColors
import io.element.android.compound.tokens.generated.internal.DarkColorTokens
import io.element.android.compound.tokens.generated.internal.DarkColorTokens.colorBlue200
import io.element.android.compound.tokens.generated.internal.DarkColorTokens.colorBlue500
import io.element.android.compound.tokens.generated.internal.DarkColorTokens.colorBlue900
import io.element.android.compound.tokens.generated.internal.DarkColorTokens.colorRed1000
import io.element.android.compound.tokens.generated.internal.DarkColorTokens.colorRed200
import io.element.android.compound.tokens.generated.internal.DarkColorTokens.colorRed300
import io.element.android.compound.tokens.generated.internal.DarkColorTokens.colorRed500
import io.element.android.compound.tokens.generated.internal.DarkColorTokens.colorRed900
import io.element.android.compound.tokens.generated.internal.LightColorTokens

val scd_fgPrimary = ScColors.colorWhite
val scd_fgSecondary = ScColors.colorWhiteAlpha_b3
val scd_fgTertiary = ScColors.colorWhiteAlpha_80
val scd_fgHint = ScColors.colorWhiteAlpha_80
val scd_fgDisabled = ScColors.colorWhiteAlpha_80
val scd_bg = ScColors.colorGray_30
val scd_bgFloating = ScColors.colorGray_42
val scd_bgDarker = ScColors.colorGray_21
val scd_bgBlack = ScColors.colorBlack
val scd_divider = ScColors.colorWhiteAlpha_1f
val scd_accent = ScColors.colorAccentGreen
const val scd_icon_alpha = 0.5f

internal val scdMaterialColorScheme = darkColorScheme(
    primary = scd_fgPrimary,
    onPrimary = scl_fgPrimary,
    primaryContainer = scd_bgDarker,
    onPrimaryContainer = scd_fgPrimary,
    inversePrimary = scl_fgPrimary,

    secondary = scd_fgSecondary,
    onSecondary = scl_fgPrimary,
    secondaryContainer = scd_bg,
    onSecondaryContainer = scd_fgSecondary,

    tertiary = scd_fgTertiary,
    onTertiary = scl_fgTertiary,
    tertiaryContainer = scd_bgBlack,
    onTertiaryContainer = scd_fgTertiary,

    background = scd_bgDarker,
    onBackground = scd_fgPrimary,
    surface = scd_bgDarker,
    onSurface = scd_fgPrimary,
    surfaceVariant = scd_bgFloating,
    onSurfaceVariant = scd_fgSecondary,
    surfaceTint = scd_bgFloating,
    inverseSurface = scl_bgFloating,
    inverseOnSurface = scl_fgPrimary,

    error = ScColors.colorAccentRed,
    onError = scd_fgPrimary,
    errorContainer = ScColors.colorAccentRed,
    onErrorContainer = scd_fgPrimary,
    outline = scd_fgTertiary,
    outlineVariant = scd_divider, // This is the divider color, as per androidx.compose.material3.DividerTokens (propagated to androidx.compose.material3.DividerDefaults.color)
    scrim = ScColors.colorBlackAlpha_1f,
)

internal val scdExposures = ScThemeExposures(
    isScTheme = true,
    horizontalDividerThickness = DividerDefaults.Thickness,
    colorOnAccent = ScColors.colorWhite,
    bubbleBgIncoming = scd_bgFloating,
    bubbleBgOutgoing = scd_bg,
    unreadBadgeColor = scd_bgFloating,
    unreadBadgeOnToolbarColor = ScColors.colorGray_61,
    appBarBg = scd_bg,
    bubbleRadius = 10.dp,
    commonLayoutRadius = 10.dp,
    timestampRadius = 6.dp,
    timestampOverlayBg = ScColors.colorBlackAlpha_80,
    unreadIndicatorLine = ScColors.colorAccentGreen,
    unreadIndicatorThickness = 2.dp,
    mentionFgLegacy = ScColors.colorWhite,
    mentionBgLegacy = ScColors.colorAccentRed,
    mentionBgOtherLegacy = ScColors.colorGray_61,
    mentionFg = ScColors.colorWhite,
    mentionBg = ScColors.colorAccentGreenAlpha_80,
    mentionFgHighlight = ScColors.colorWhite,
    mentionBgHighlight = ScColors.colorAccentRed,
    greenFg = ScColors.colorAccentGreen,
    greenBg = ScColors.colorAccentGreenAlpha_30,
    messageHighlightBg = ScColors.colorAccentGreenAlpha_80,
    composerBlockBg = scd_bgFloating,
    composerBlockFg = scd_fgPrimary,
    spaceBarBg = scd_bg,
)

@OptIn(CoreColorToken::class)
internal val scdSemanticColors = SemanticColors(
    textPrimary = scd_fgPrimary,
    textSecondary = scd_fgSecondary,
    textDisabled = scd_fgDisabled,
    textActionPrimary = scd_fgPrimary,
    textActionAccent = scd_accent,
    textLinkExternal = ScColors.colorAccentBlue,
    textCriticalPrimary = ScColors.colorAccentRed,
    textSuccessPrimary = ScColors.colorAccentGreen,
    textInfoPrimary = ScColors.colorAccentBlueLight,
    textOnSolidPrimary = scl_fgPrimary,
    textBadgeInfo = scd_fgPrimary,
    textBadgeAccent = scd_fgPrimary,
    bgSubtlePrimary = scd_bg,
    bgSubtleSecondary =  scd_bgFloating,
    bgSubtleSecondaryLevel0 =  scd_bg,
    bgCanvasDefault = scd_bg,
    bgCanvasDefaultLevel1 = scd_bgFloating,
    bgCanvasDisabled = scd_bgDarker,
    bgActionPrimaryRest = scd_fgPrimary,
    bgActionPrimaryHovered = scd_fgSecondary,
    bgActionPrimaryPressed = scd_fgSecondary,
    bgActionPrimaryDisabled = scd_fgHint,
    bgActionSecondaryRest = scd_bg,
    bgActionSecondaryHovered = scd_bgFloating,
    bgActionSecondaryPressed = scd_bgFloating,
    bgActionTertiaryRest = scd_bg, // TODO?
    bgActionTertiaryHovered = scd_bgFloating, // TODO?
    bgActionTertiarySelected = scd_bgFloating, // TODO?
    bgBadgeAccent = ScColors.colorAccentGreenAlpha_30,
    bgBadgeDefault = ScColors.colorGray_61,
    bgBadgeInfo = DarkColorTokens.colorAlphaBlue300, // TODO?
    bgCriticalPrimary = colorRed900, // TODO
    bgCriticalHovered = colorRed1000, // TODO
    bgCriticalSubtle = colorRed200, // TODO
    bgCriticalSubtleHovered = colorRed300, // TODO
    borderAccentSubtle = scd_accent,
    bgSuccessSubtle = ScColors.colorAccentGreen.copy(alpha=0.2f),
    bgInfoSubtle = ScColors.colorAccentBlueDark,
    borderDisabled = scd_divider,
    borderFocused = colorBlue900, // TODO
    borderInteractivePrimary = scd_fgSecondary,
    borderInteractiveSecondary = scd_fgTertiary,
    borderInteractiveHovered = scd_fgPrimary,
    borderCriticalPrimary = colorRed900, // TODO
    borderCriticalHovered = colorRed1000, // TODO
    borderCriticalSubtle = colorRed500, // TODO
    borderSuccessSubtle = ScColors.colorAccentGreen,
    borderInfoSubtle = ScColors.colorAccentBlueDark,
    iconPrimary = scd_fgPrimary,
    iconSecondary = scd_fgSecondary,
    iconTertiary = scd_fgTertiary,
    iconQuaternary = scd_fgTertiary,
    iconDisabled = scd_fgDisabled,
    iconPrimaryAlpha = scd_fgPrimary.copy(alpha = scd_icon_alpha),
    iconSecondaryAlpha = scd_fgSecondary.copy(alpha = scd_icon_alpha),
    iconTertiaryAlpha = scd_fgTertiary.copy(alpha = scd_icon_alpha),
    iconQuaternaryAlpha = scd_fgTertiary.copy(alpha = scd_icon_alpha),
    iconAccentPrimary = scd_accent,
    iconAccentTertiary = scd_accent,
    iconCriticalPrimary = ScColors.colorAccentRed, // TODO align with other colorRed900?
    iconSuccessPrimary = ScColors.colorAccentGreen,
    iconInfoPrimary = ScColors.colorAccentBlue,
    iconOnSolidPrimary = scl_fgPrimary,
    bgAccentRest = scd_accent,
    bgAccentSelected = scd_accent,
    bgAccentHovered = scd_accent,
    bgAccentPressed = scd_accent,
    // TODO-start
    gradientActionStop1 = DarkColorTokens.colorGreen1100,
    gradientActionStop2 = DarkColorTokens.colorGreen900,
    gradientActionStop3 = DarkColorTokens.colorGreen700,
    gradientActionStop4 = DarkColorTokens.colorGreen500,
    gradientInfoStop1 = DarkColorTokens.colorAlphaBlue500,
    gradientInfoStop2 = DarkColorTokens.colorAlphaBlue400,
    gradientInfoStop3 = DarkColorTokens.colorAlphaBlue300,
    gradientInfoStop4 = DarkColorTokens.colorAlphaBlue200,
    gradientInfoStop5 = DarkColorTokens.colorAlphaBlue100,
    gradientInfoStop6 = DarkColorTokens.colorTransparent,
    gradientSubtleStop1 = DarkColorTokens.colorAlphaGreen500,
    gradientSubtleStop2 = DarkColorTokens.colorAlphaGreen400,
    gradientSubtleStop3 = DarkColorTokens.colorAlphaGreen300,
    gradientSubtleStop4 = DarkColorTokens.colorAlphaGreen200,
    gradientSubtleStop5 = DarkColorTokens.colorAlphaGreen100,
    gradientSubtleStop6 = DarkColorTokens.colorTransparent,
    // yes, upstream has light tokens for dark theme here as well, as of compound v0.0.6
    bgDecorative1 = LightColorTokens.colorLime300,
    bgDecorative2 = LightColorTokens.colorCyan300,
    bgDecorative3 = LightColorTokens.colorFuchsia300,
    bgDecorative4 = LightColorTokens.colorPurple300,
    bgDecorative5 = LightColorTokens.colorPink300,
    bgDecorative6 = LightColorTokens.colorOrange300,
    textDecorative1 = LightColorTokens.colorLime1100,
    textDecorative2 = LightColorTokens.colorCyan1100,
    textDecorative3 = LightColorTokens.colorFuchsia1100,
    textDecorative4 = LightColorTokens.colorPurple1100,
    textDecorative5 = LightColorTokens.colorPink1100,
    textDecorative6 = LightColorTokens.colorOrange1100,
    // TODO-end
    isLight = false,
)
