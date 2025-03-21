/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.theme

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import chat.schildi.lib.preferences.DefaultScPreferencesStore
import chat.schildi.lib.preferences.LocalScPreferencesStore
import chat.schildi.lib.preferences.ScPreferencesStore
import chat.schildi.theme.ScTheme
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.theme.Theme
import io.element.android.compound.theme.isDark
import io.element.android.compound.theme.mapToTheme
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.core.meta.BuildType
import io.element.android.libraries.core.meta.ScBuildMeta
import io.element.android.libraries.preferences.api.store.AppPreferencesStore

val LocalBuildMeta = staticCompositionLocalOf {
    BuildMeta(
        scBuildMeta = ScBuildMeta("1.2.3", "ex0", null),
        isDebuggable = true,
        buildType = BuildType.DEBUG,
        applicationName = "MyApp",
        productionApplicationName = "MyAppProd",
        desktopApplicationName = "MyAppDesktop",
        applicationId = "AppId",
        isEnterpriseBuild = false,
        lowPrivacyLoggingEnabled = false,
        versionName = "aVersion",
        versionCode = 123,
        gitRevision = "aRevision",
        gitBranchName = "aBranch",
        flavorDescription = "aFlavor",
        flavorShortDescription = "aFlavorShort",
    )
}

/**
 * Theme to use for all the regular screens of the application.
 * Will manage the light / dark theme based on the user preference.
 * Will also ensure that the system is applying the correct global theme
 * to the application, especially when the system is light and the application
 * is forced to use dark theme.
 */
@Composable
fun ElementThemeApp(
    appPreferencesStore: AppPreferencesStore,
    scPreferencesStore: ScPreferencesStore = rememberDefaultScPreferencesStore(), // SC
    enterpriseService: EnterpriseService,
    buildMeta: BuildMeta,
    content: @Composable () -> Unit,
) {
    val theme by remember {
        appPreferencesStore.getThemeFlow().mapToTheme()
    }
        .collectAsState(initial = Theme.System)
    LaunchedEffect(theme) {
        AppCompatDelegate.setDefaultNightMode(
            when (theme) {
                Theme.System -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                Theme.Light -> AppCompatDelegate.MODE_NIGHT_NO
                Theme.Dark -> AppCompatDelegate.MODE_NIGHT_YES
            }
        )
    }
    val compoundLight = remember { enterpriseService.semanticColorsLight() }
    val compoundDark = remember { enterpriseService.semanticColorsDark() }
    CompositionLocalProvider(
        LocalBuildMeta provides buildMeta,
        LocalScPreferencesStore provides scPreferencesStore,
    ) {
        ScTheme(
            darkTheme = theme.isDark(),
            content = content,
            //compoundLight = compoundLight, // SC: eh don't care about enterprise colors
            //compoundDark = compoundDark, // SC commented out
        )
    }
}

