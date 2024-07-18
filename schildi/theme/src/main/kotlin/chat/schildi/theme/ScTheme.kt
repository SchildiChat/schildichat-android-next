package chat.schildi.theme

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import chat.schildi.lib.preferences.ScPrefs
import chat.schildi.lib.preferences.value
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.theme.ForcedDarkElementTheme
import io.element.android.compound.tokens.generated.SemanticColors
import io.element.android.compound.tokens.sc.ElTypographyTokens
import io.element.android.compound.tokens.sc.ExposedTypographyTokens

object ScTheme {
    val exposures: ScThemeExposures
        @Composable
        @ReadOnlyComposable
        get() = LocalScExposures.current

    val yes: Boolean
        @Composable
        @ReadOnlyComposable
        get() = exposures.isScTheme

    val scTimeline: Boolean
        @Composable
        get() = ScPrefs.SC_TIMELINE_LAYOUT.value()
}

// Element defaults to light compound colors, so follow that as fallback default for exposures as well
internal val LocalScExposures = staticCompositionLocalOf { elementLightScExposures }

fun getThemeExposures(darkTheme: Boolean, useScTheme: Boolean) = when {
    darkTheme && useScTheme -> scdExposures
    !darkTheme && useScTheme -> sclExposures
    darkTheme && !useScTheme -> elementDarkScExposures
    else -> elementLightScExposures
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun ScTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    applySystemBarsUpdate: Boolean = true,
    lightStatusBar: Boolean = !darkTheme,
    dynamicColor: Boolean = ScPrefs.SC_DYNAMICCOLORS.value(), /* true to enable MaterialYou */
    useScTheme: Boolean = ScPrefs.SC_THEME.value(),
    useElTypography: Boolean = ScPrefs.EL_TYPOGRAPHY.value(),
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val compoundColors: SemanticColors
    val materialColors: ColorScheme
    if (useScTheme && !dynamicColor)  {
        compoundColors = if (darkTheme) scdSemanticColors else sclSemanticColors
        materialColors = if (darkTheme) scdMaterialColorScheme else sclMaterialColorScheme
    } else {if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val dynamicColorScheme = if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        compoundColors = provideDynamicColorScheme(dynamicColorScheme, darkTheme)
        materialColors = dynamicColorScheme
    } else {
        compoundColors = if (darkTheme) elColorsDark else elColorsLight
        materialColors = if (darkTheme) elMaterialColorSchemeDark else elMaterialColorSchemeLight
    }
    }

    val typography = if (useElTypography) elTypography else scTypography
    val typographyTokens = if (useElTypography) ElTypographyTokens else ScTypographyTokens

    val currentExposures = remember {
        // EleLight is default
        elementLightScExposures.copy()
    }.apply { updateColorsFrom(getThemeExposures(darkTheme, useScTheme)) }

    CompositionLocalProvider(
        LocalScExposures provides currentExposures
    ) {
        ElementTheme(
            darkTheme = darkTheme,
            applySystemBarsUpdate = applySystemBarsUpdate,
            lightStatusBar = lightStatusBar,
            dynamicColor = dynamicColor,
            compoundColors = compoundColors,
            materialColors = materialColors,
            typography = typography,
            typographyTokens = typographyTokens,
            content = content,
        )
    }
}

@Composable
fun provideDynamicColorScheme(colorScheme: ColorScheme, isLight: Boolean): SemanticColors {
    return SemanticColors(
        textPrimary = colorScheme.primary,
        textSecondary = colorScheme.secondary,
        textPlaceholder = colorScheme.onSurface.copy(alpha = 0.4f),
        textDisabled = colorScheme.onSurface.copy(alpha = 0.38f),
        textActionPrimary = colorScheme.primary,
        textActionAccent = colorScheme.tertiary,
        textLinkExternal = colorScheme.secondary,
        textCriticalPrimary = colorScheme.error,
        textSuccessPrimary = colorScheme.primary,
        textInfoPrimary = colorScheme.onBackground,
        textOnSolidPrimary = colorScheme.onPrimary,
        bgSubtlePrimary = colorScheme.surface,
        bgSubtleSecondary = colorScheme.surfaceVariant,
        bgCanvasDefault = colorScheme.background,
        bgCanvasDisabled = colorScheme.surface,
        bgActionPrimaryRest = colorScheme.primary,
        bgActionPrimaryHovered = colorScheme.primaryContainer,
        bgActionPrimaryPressed = colorScheme.primaryContainer,
        bgActionPrimaryDisabled = colorScheme.onSurface.copy(alpha = 0.38f),
        bgActionSecondaryRest = colorScheme.surface,
        bgActionSecondaryHovered = colorScheme.onSurface,
        bgActionSecondaryPressed = colorScheme.onSurface,
        bgCriticalPrimary = colorScheme.errorContainer,
        bgCriticalHovered = colorScheme.errorContainer,
        bgCriticalSubtle = colorScheme.error,
        bgCriticalSubtleHovered = colorScheme.error,
        bgSuccessSubtle = colorScheme.secondary.copy(alpha = 0.2f),
        bgInfoSubtle = colorScheme.tertiary,
        borderDisabled = colorScheme.onSurface.copy(alpha = 0.12f),
        borderFocused = colorScheme.tertiary,
        borderInteractivePrimary = colorScheme.secondary,
        borderInteractiveSecondary = colorScheme.onSurface.copy(alpha = 0.60f),
        borderInteractiveHovered = colorScheme.onSurface,
        borderCriticalPrimary = colorScheme.error,
        borderCriticalHovered = colorScheme.error,
        borderCriticalSubtle = colorScheme.error.copy(alpha = 0.38f),
        borderSuccessSubtle = colorScheme.secondary,
        borderInfoSubtle = colorScheme.tertiary,
        iconPrimary = colorScheme.primary,
        iconSecondary = colorScheme.secondary,
        iconTertiary = colorScheme.tertiary,
        iconQuaternary = colorScheme.onSurface,
        iconDisabled = colorScheme.onSurface.copy(alpha = 0.38f),
        iconPrimaryAlpha = colorScheme.primary.copy(alpha = 0.8f),
        iconSecondaryAlpha = colorScheme.secondary.copy(alpha = 0.8f),
        iconTertiaryAlpha = colorScheme.tertiary.copy(alpha = 0.8f),
        iconQuaternaryAlpha = colorScheme.onSurface.copy(alpha = 0.8f),
        iconAccentTertiary = colorScheme.secondary,
        iconCriticalPrimary = colorScheme.error,
        iconSuccessPrimary = colorScheme.secondary,
        iconInfoPrimary = colorScheme.tertiary,
        iconOnSolidPrimary = colorScheme.onPrimary,
        bgAccentRest = colorScheme.primaryContainer,
        bgAccentHovered = colorScheme.primary,
        bgAccentPressed = colorScheme.primary,
        bgDecorative1 = colorScheme.tertiaryContainer,
        bgDecorative2 = colorScheme.secondaryContainer,
        bgDecorative3 = colorScheme.errorContainer,
        bgDecorative4 = colorScheme.onPrimaryContainer,
        bgDecorative5 = colorScheme.onSecondaryContainer,
        bgDecorative6 = colorScheme.onTertiaryContainer,
        textDecorative1 = colorScheme.primary,
        textDecorative2 = colorScheme.secondary,
        textDecorative3 = colorScheme.error,
        textDecorative4 = colorScheme.onPrimary,
        textDecorative5 = colorScheme.onSecondary,
        textDecorative6 = colorScheme.onTertiary,
        isLight = isLight,
    )
}


/**
 * Can be used to force a composable in dark theme.
 * It will automatically change the system ui colors back to normal when leaving the composition.
 */
@Composable
fun ForcedDarkScTheme(
    lightStatusBar: Boolean = false,
    useScTheme: Boolean = ScPrefs.SC_THEME.value(),
    dynamicColor: Boolean = ScPrefs.SC_DYNAMICCOLORS.value(),
    content: @Composable () -> Unit,
) {
    val currentExposures = remember {
        // EleLight is default
        elementLightScExposures.copy()
    }.apply { updateColorsFrom(getThemeExposures(true, useScTheme)) }
    CompositionLocalProvider(
        LocalScExposures provides currentExposures
    ) {
        ForcedDarkElementTheme(
            lightStatusBar = lightStatusBar,
            content = content,
        )
    }

    // Function to provide dynamic SemanticColors based on the color scheme

    /* TODO if !useScTheme do other stuffs
    val systemUiController = rememberSystemUiController()
    val colorScheme = MaterialTheme.colorScheme
    val wasDarkTheme = !ElementTheme.colors.isLight
    DisposableEffect(Unit) {
        onDispose {
            systemUiController.applyTheme(colorScheme, wasDarkTheme)
        }
    }
    ElementTheme(darkTheme = true, lightStatusBar = lightStatusBar, content = content)
     */
}

// Calculate the color as if with alpha on white background
fun Color.fakeAlpha(alpha: Float) = Color(
    1f - alpha * (1f - red),
    1f - alpha * (1f - green),
    1f - alpha * (1f - blue),
    1f,
)

val ExposedTypographyTokens.scBubbleFont
    @Composable
    get() = if (ScPrefs.EL_TYPOGRAPHY.value()) fontBodyLgRegular else fontBodyMdRegular
