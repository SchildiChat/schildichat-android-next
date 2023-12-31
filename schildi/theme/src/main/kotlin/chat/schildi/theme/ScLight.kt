package chat.schildi.theme

import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.unit.dp
import io.element.android.compound.annotations.CoreColorToken
import io.element.android.compound.tokens.generated.SemanticColors
import io.element.android.compound.tokens.generated.internal.LightColorTokens

val scl_fgPrimary = ScColors.colorBlackAlpha_de
val scl_fgSecondary = ScColors.colorBlackAlpha_8a
val scl_fgTertiary = ScColors.colorBlackAlpha_4c
val scl_fgHint = ScColors.colorBlackAlpha_4c
val scl_fgDisabled = ScColors.colorBlackAlpha_4c
val scl_bg = ScColors.colorWhite_fa
val scl_bgFloating = ScColors.colorWhite
val scl_bgDarker = ScColors.colorWhite_fa
val scl_bgBlack = ScColors.colorWhite_ee
val scl_divider = ScColors.colorBlackAlpha_1f
val scl_accent = ScColors.colorAccentGreen
const val scl_icon_alpha = 0.5f

internal val sclMaterialColorScheme = lightColorScheme(
    primary = scl_fgPrimary,
    onPrimary = scd_fgPrimary,
    primaryContainer = scl_bg,
    onPrimaryContainer = scl_fgPrimary,
    inversePrimary = scl_bg,

    secondary = scl_fgSecondary,
    onSecondary = scl_fgPrimary,
    secondaryContainer = scl_bg,
    onSecondaryContainer = scl_fgSecondary,

    tertiary = scl_fgTertiary,
    onTertiary = scl_fgTertiary,
    tertiaryContainer = scl_bgBlack,
    onTertiaryContainer = scl_fgTertiary,

    background = scl_bg,
    onBackground = scl_fgPrimary,
    surface = scl_bg,
    onSurface = scl_fgPrimary,
    surfaceVariant = scl_bgFloating,
    onSurfaceVariant = scl_fgPrimary,
    surfaceTint = scl_bgFloating,
    inverseSurface = scd_bg,
    inverseOnSurface = scd_fgPrimary,

    error = ScColors.colorAccentRed,
    onError = scl_fgPrimary,
    errorContainer = ScColors.colorAccentRed,
    onErrorContainer = scl_fgPrimary,
    outline = scl_fgTertiary,
    outlineVariant = scl_divider, // This is the divider color, as per androidx.compose.material3.DividerTokens (propagated to androidx.compose.material3.DividerDefaults.color)
    scrim = ScColors.colorBlackAlpha_1f,
)

internal val sclExposures = ScThemeExposures(
    isScTheme = true,
    horizontalDividerThickness = DividerDefaults.Thickness,
    colorOnAccent = ScColors.colorWhite,
    bubbleBgIncoming = ScColors.colorWhite_ee,
    bubbleBgOutgoing = scl_accent.fakeAlpha(0.12f),
    appBarBg = scl_bg,
    bubbleRadius = 10.dp,
    timestampRadius = 6.dp,
    timestampOverlayBg = ScColors.colorWhiteAlpha_b3,
)

@OptIn(CoreColorToken::class)
internal val sclSemanticColors = SemanticColors(
    textPrimary = scl_fgPrimary,
    textSecondary = scl_fgSecondary,
    textPlaceholder = scl_fgHint,
    textDisabled = scl_fgDisabled,
    textActionPrimary = scl_fgPrimary,
    textActionAccent = scl_accent,
    textLinkExternal = ScColors.colorAccentBlue,
    textCriticalPrimary = ScColors.colorAccentRed,
    textSuccessPrimary = ScColors.colorAccentGreen,
    textInfoPrimary = ScColors.colorAccentBlueLight,
    textOnSolidPrimary = scl_fgPrimary,
    bgSubtlePrimary = scl_bgDarker,
    bgSubtleSecondary =  scl_bgBlack,
    bgCanvasDefault = scl_bg,
    bgCanvasDisabled = scl_bgDarker,
    bgActionPrimaryRest = scl_fgPrimary,
    bgActionPrimaryHovered = scl_fgSecondary,
    bgActionPrimaryPressed = scl_fgSecondary,
    bgActionPrimaryDisabled = scl_fgHint,
    bgActionSecondaryRest = scl_bg,
    bgActionSecondaryHovered = scl_bgFloating,
    bgActionSecondaryPressed = scl_bgFloating,
    bgCriticalPrimary = LightColorTokens.colorRed900, // TODO
    bgCriticalHovered = LightColorTokens.colorRed1000, // TODO
    bgCriticalSubtle = LightColorTokens.colorRed200, // TODO
    bgCriticalSubtleHovered = LightColorTokens.colorRed300, // TODO
    bgSuccessSubtle = ScColors.colorAccentGreen.copy(alpha=0.2f),
    bgInfoSubtle = LightColorTokens.colorBlue200, // TODO
    borderDisabled = scl_divider,
    borderFocused = LightColorTokens.colorBlue900, // TODO
    borderInteractivePrimary = scl_fgSecondary,
    borderInteractiveSecondary = scl_fgTertiary,
    borderInteractiveHovered = scl_fgPrimary,
    borderCriticalPrimary = LightColorTokens.colorRed900, // TODO
    borderCriticalHovered = LightColorTokens.colorRed1000, // TODO
    borderCriticalSubtle = LightColorTokens.colorRed500, // TODO
    borderSuccessSubtle = ScColors.colorAccentGreen,
    borderInfoSubtle = LightColorTokens.colorBlue500, // TODO
    iconPrimary = scl_fgPrimary,
    iconSecondary = scl_fgSecondary,
    iconTertiary = scl_fgSecondary, // This is used as default in ListItem leading icons, i.e. in PreferencesRootView
    iconQuaternary = scl_fgTertiary,
    iconDisabled = scl_fgDisabled,
    iconPrimaryAlpha = scl_fgPrimary.copy(alpha = scl_icon_alpha),
    iconSecondaryAlpha = scl_fgSecondary.copy(alpha = scl_icon_alpha),
    iconTertiaryAlpha = scl_fgTertiary.copy(alpha = scl_icon_alpha),
    iconQuaternaryAlpha = scl_fgTertiary.copy(alpha = scl_icon_alpha),
    iconAccentTertiary = scl_accent,
    iconCriticalPrimary = ScColors.colorAccentRed, // TODO align with other colorRed900?
    iconSuccessPrimary = ScColors.colorAccentGreen,
    iconInfoPrimary = ScColors.colorAccentBlue,
    iconOnSolidPrimary = scl_fgPrimary,
    isLight = true,
)
