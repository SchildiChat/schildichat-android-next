package chat.schildi.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import chat.schildi.lib.preferences.ScPrefs
import chat.schildi.lib.preferences.value
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.theme.ForcedDarkElementTheme
import io.element.android.compound.theme.toMaterialColorScheme
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

@Composable
fun ScTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    applySystemBarsUpdate: Boolean = true,
    lightStatusBar: Boolean = !darkTheme,
    dynamicColor: Boolean = false, /* true to enable MaterialYou */
    useScTheme: Boolean = ScPrefs.SC_THEME.value(),
    useElTypography: Boolean = ScPrefs.EL_TYPOGRAPHY.value(),
    content: @Composable () -> Unit,
) {
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
            compoundLight = if (useScTheme) sclSemanticColors else elColorsLight,
            compoundDark = if (useScTheme) scdSemanticColors else elColorsDark,
            materialColorsLight = if (useScTheme) sclMaterialColorScheme else elMaterialColorSchemeLight,
            materialColorsDark = if (useScTheme) scdMaterialColorScheme else elMaterialColorSchemeDark,
            typography = typography,
            typographyTokens = typographyTokens,
            content = content,
        )
    }
}

/**
 * Can be used to force a composable in dark theme.
 * It will automatically change the system ui colors back to normal when leaving the composition.
 */
@Composable
fun ForcedDarkScTheme(
    lightStatusBar: Boolean = false,
    useScTheme: Boolean = ScPrefs.SC_THEME.value(),
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
