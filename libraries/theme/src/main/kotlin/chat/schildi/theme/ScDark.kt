package chat.schildi.theme

import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.darkColorScheme
import io.element.android.libraries.theme.compound.generated.SemanticColors
import io.element.android.libraries.theme.compound.generated.internal.DarkDesignTokens

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
    onSurfaceVariant = scd_fgPrimary,
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
    bubbleBgIncoming = scd_bgFloating,
    bubbleBgOutgoing = scd_bg,
    appBarBg = scd_bg,
)

internal val scdSemanticColors = SemanticColors(
    scThemeExposures = scdExposures,
    textPrimary = scd_fgPrimary,
    textSecondary = scd_fgSecondary,
    textPlaceholder = scd_fgHint,
    textDisabled = scd_fgDisabled,
    textActionPrimary = scd_fgPrimary,
    textActionAccent = scd_accent,
    textLinkExternal = ScColors.colorAccentBlue,
    textCriticalPrimary = ScColors.colorAccentRed,
    textSuccessPrimary = ScColors.colorAccentGreen,
    textInfoPrimary = ScColors.colorAccentBlueLight,
    textOnSolidPrimary = scl_fgPrimary,
    bgSubtlePrimary = scd_bg,
    bgSubtleSecondary =  scd_bgFloating,
    bgCanvasDefault = scd_bg,
    bgCanvasDisabled = scd_bgDarker,
    bgActionPrimaryRest = scd_fgPrimary,
    bgActionPrimaryHovered = scd_fgSecondary,
    bgActionPrimaryPressed = scd_fgSecondary,
    bgActionPrimaryDisabled = scd_fgHint,
    bgActionSecondaryRest = scd_bg,
    bgActionSecondaryHovered = scd_bgFloating,
    bgActionSecondaryPressed = scd_bgFloating,
    // TODO from here
    bgCriticalPrimary = DarkDesignTokens.colorRed900,
    bgCriticalHovered = DarkDesignTokens.colorRed1000,
    bgCriticalSubtle = DarkDesignTokens.colorRed200,
    bgCriticalSubtleHovered = DarkDesignTokens.colorRed300,
    bgSuccessSubtle = ScColors.colorAccentGreen.copy(alpha=0.2f),
    bgInfoSubtle = DarkDesignTokens.colorBlue200,
    borderDisabled = DarkDesignTokens.colorGray500,
    borderFocused = DarkDesignTokens.colorBlue900,
    borderInteractivePrimary = DarkDesignTokens.colorGray800,
    borderInteractiveSecondary = DarkDesignTokens.colorGray600,
    borderInteractiveHovered = DarkDesignTokens.colorGray1100,
    borderCriticalPrimary = DarkDesignTokens.colorRed900,
    borderCriticalHovered = DarkDesignTokens.colorRed1000,
    borderCriticalSubtle = DarkDesignTokens.colorRed500,
    borderSuccessSubtle = ScColors.colorAccentGreen,
    borderInfoSubtle = DarkDesignTokens.colorBlue500,
    iconPrimary = DarkDesignTokens.colorGray1400,
    iconSecondary = DarkDesignTokens.colorGray900,
    iconTertiary = DarkDesignTokens.colorGray800,
    iconQuaternary = DarkDesignTokens.colorGray700,
    iconDisabled = DarkDesignTokens.colorGray700,
    iconPrimaryAlpha = DarkDesignTokens.colorAlphaGray1400,
    iconSecondaryAlpha = DarkDesignTokens.colorAlphaGray900,
    iconTertiaryAlpha = DarkDesignTokens.colorAlphaGray800,
    iconQuaternaryAlpha = DarkDesignTokens.colorAlphaGray700,
    iconAccentTertiary = scd_accent,
    iconCriticalPrimary = DarkDesignTokens.colorRed900,
    iconSuccessPrimary = DarkDesignTokens.colorGreen900,
    iconInfoPrimary = DarkDesignTokens.colorBlue900,
    iconOnSolidPrimary = DarkDesignTokens.colorThemeBg,
    isLight = false,
)
