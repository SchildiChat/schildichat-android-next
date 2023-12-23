package chat.schildi.theme

import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.darkColorScheme
import io.element.android.compound.annotations.CoreColorToken
import io.element.android.compound.tokens.generated.SemanticColors
import io.element.android.compound.tokens.generated.internal.DarkColorTokens.colorBlue200
import io.element.android.compound.tokens.generated.internal.DarkColorTokens.colorBlue500
import io.element.android.compound.tokens.generated.internal.DarkColorTokens.colorBlue900
import io.element.android.compound.tokens.generated.internal.DarkColorTokens.colorRed1000
import io.element.android.compound.tokens.generated.internal.DarkColorTokens.colorRed200
import io.element.android.compound.tokens.generated.internal.DarkColorTokens.colorRed300
import io.element.android.compound.tokens.generated.internal.DarkColorTokens.colorRed500
import io.element.android.compound.tokens.generated.internal.DarkColorTokens.colorRed900

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
val scd_icon_alpha = 0.5f

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

@OptIn(CoreColorToken::class)
internal val scdSemanticColors = SemanticColors(
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
    bgCriticalPrimary = colorRed900, // TODO
    bgCriticalHovered = colorRed1000, // TODO
    bgCriticalSubtle = colorRed200, // TODO
    bgCriticalSubtleHovered = colorRed300, // TODO
    bgSuccessSubtle = ScColors.colorAccentGreen.copy(alpha=0.2f),
    bgInfoSubtle = colorBlue200, // TODO
    borderDisabled = scd_divider,
    borderFocused = colorBlue900, // TODO
    borderInteractivePrimary = scd_divider, // TODO?
    borderInteractiveSecondary = scd_divider, // TODO?
    borderInteractiveHovered = scd_divider, // TODO?
    borderCriticalPrimary = colorRed900, // TODO
    borderCriticalHovered = colorRed1000, // TODO
    borderCriticalSubtle = colorRed500, // TODO
    borderSuccessSubtle = ScColors.colorAccentGreen,
    borderInfoSubtle = colorBlue500, // TODO
    iconPrimary = scd_fgPrimary,
    iconSecondary = scd_fgSecondary,
    iconTertiary = scd_fgTertiary,
    iconQuaternary = scd_fgTertiary,
    iconDisabled = scd_fgDisabled,
    iconPrimaryAlpha = scd_fgPrimary.copy(alpha = scd_icon_alpha),
    iconSecondaryAlpha = scd_fgSecondary.copy(alpha = scd_icon_alpha),
    iconTertiaryAlpha = scd_fgTertiary.copy(alpha = scd_icon_alpha),
    iconQuaternaryAlpha = scd_fgTertiary.copy(alpha = scd_icon_alpha),
    iconAccentTertiary = scd_accent,
    iconCriticalPrimary = ScColors.colorAccentRed, // TODO align with other colorRed900?
    iconSuccessPrimary = ScColors.colorAccentGreen,
    iconInfoPrimary = ScColors.colorAccentBlue,
    iconOnSolidPrimary = scd_fgPrimary,
    isLight = false,
)
