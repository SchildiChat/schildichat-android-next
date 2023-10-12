/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import chat.schildi.lib.preferences.scPrefs
import chat.schildi.theme.scTypography
import chat.schildi.theme.scdMaterialColorScheme
import chat.schildi.theme.scdSemanticColors
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import io.element.android.libraries.theme.compound.compoundColorsDark
import io.element.android.libraries.theme.compound.compoundColorsLight
import io.element.android.libraries.theme.compound.compoundTypography
import io.element.android.libraries.theme.compound.generated.SemanticColors
import io.element.android.libraries.theme.compound.generated.TypographyTokens

/**
 * Inspired from https://medium.com/@lucasyujideveloper/54cbcbde1ace
 */
object ElementTheme {
    /**
     * The current [SemanticColors] provided by [ElementTheme].
     * These come from Compound and are the recommended colors to use for custom components.
     * In Figma, these colors usually have the `Light/` or `Dark/` prefix.
     */
    val colors: SemanticColors
        @Composable
        @ReadOnlyComposable
        get() = LocalCompoundColors.current

    /**
     * The current Material 3 [ColorScheme] provided by [ElementTheme], coming from [MaterialTheme].
     * In Figma, these colors usually have the `M3/` prefix.
     */
    val materialColors: ColorScheme
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme

    /**
     * Compound [Typography] tokens. In Figma, these have the `Android/font/` prefix.
     */
    val typography: TypographyTokens = TypographyTokens

    /**
     * Material 3 [Typography] tokens. In Figma, these have the `M3 Typography/` prefix.
     */
    val materialTypography: Typography
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography

    /**
     * Returns whether the theme version used is the light or the dark one.
     */
    val isLightTheme: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalCompoundColors.current.isLight

    val isScTheme: Boolean
        @Composable
        @ReadOnlyComposable
        get() = colors.scThemeExposures.isScTheme
}

/* Global variables (application level) */
internal val LocalCompoundColors = staticCompositionLocalOf { compoundColorsLight }

@Composable
fun ElementTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    lightStatusBar: Boolean = !darkTheme,
    dynamicColor: Boolean = false, /* true to enable MaterialYou */
    elCompoundColors: SemanticColors = if (darkTheme) compoundColorsDark else compoundColorsLight,
    elMaterialLightColors: ColorScheme = materialColorSchemeLight,
    elMaterialDarkColors: ColorScheme = materialColorSchemeDark,
    scCompoundColors: SemanticColors = if (darkTheme) scdSemanticColors else compoundColorsLight,
    scMaterialLightColors: ColorScheme = materialColorSchemeLight,
    scMaterialDarkColors: ColorScheme = scdMaterialColorScheme,
    elTypography: Typography = compoundTypography,
    content: @Composable () -> Unit,
) {
    // SC theming
    val scPrefs = scPrefs()
    val compoundColors: SemanticColors
    val materialLightColors: ColorScheme
    val materialDarkColors: ColorScheme
    val useScTheme = scPrefs.settingState(scPrefs.SC_THEME).value
    if (useScTheme) {
        compoundColors = scCompoundColors
        materialLightColors = scMaterialLightColors
        materialDarkColors = scMaterialDarkColors
    } else {
        compoundColors = elCompoundColors
        materialLightColors = elMaterialLightColors
        materialDarkColors = elMaterialDarkColors
    }
    val useElTypography = scPrefs.settingState(scPrefs.EL_TYPOGRAPHY).value
    val typography = if (useElTypography) elTypography else scTypography
    // SC theming end

    val systemUiController = rememberSystemUiController()
    val currentCompoundColor = remember(darkTheme) {
        compoundColors.copy()
    }.apply { updateColorsFrom(compoundColors) }
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> materialDarkColors
        else -> materialLightColors
    }
    val statusBarColorScheme = if (lightStatusBar) {
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                dynamicLightColorScheme(context)
            }
            else -> materialLightColors
        }
    } else {
        colorScheme
    }
    SideEffect {
        systemUiController.applyTheme(colorScheme = statusBarColorScheme, darkTheme = darkTheme && !lightStatusBar)
    }
    CompositionLocalProvider(
        LocalCompoundColors provides currentCompoundColor,
        LocalContentColor provides colorScheme.onSurface,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}

internal fun SystemUiController.applyTheme(
    colorScheme: ColorScheme,
    darkTheme: Boolean,
) {
    val useDarkIcons = !darkTheme
    setStatusBarColor(
        color = colorScheme.background
    )
    setSystemBarsColor(
        color = Color.Transparent,
        darkIcons = useDarkIcons
    )
}
