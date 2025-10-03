/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.compound.theme

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext

/**
 * Can be used to force a composable in dark theme.
 * It will automatically change the system ui colors back to normal when leaving the composition.
 */
@Composable
fun ForcedDarkElementTheme(
    lightStatusBar: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val wasDarkTheme = !ElementTheme.colors.isLight
    val activity = LocalContext.current as? ComponentActivity
    DisposableEffect(Unit) {
        onDispose {
            activity?.enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.auto(
                    lightScrim = colorScheme.background.toArgb(),
                    darkScrim = colorScheme.background.toArgb(),
                ),
                navigationBarStyle = if (wasDarkTheme) {
                    SystemBarStyle.dark(Color.Transparent.toArgb())
                } else {
                    SystemBarStyle.light(
                        scrim = Color.Transparent.toArgb(),
                        darkScrim = Color.Transparent.toArgb()
                    )
                }
            )
        }
    }
    ElementTheme(darkTheme = true, lightStatusBar = lightStatusBar, content = content)
}
