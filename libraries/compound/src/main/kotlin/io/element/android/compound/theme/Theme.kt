/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.compound.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class Theme {
    System,
    Dark,
    Light;
}

val themes = listOf(Theme.System, Theme.Dark, Theme.Light)

@Composable
fun Theme.isDark(): Boolean {
    return when (this) {
        Theme.System -> isSystemInDarkTheme()
        Theme.Dark -> true
        Theme.Light -> false
    }
}

fun Flow<String?>.mapToTheme(): Flow<Theme> = map {
    when (it) {
        null -> Theme.System
        else -> Theme.valueOf(it)
    }
}
