/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.designsystem.colors

import androidx.collection.LruCache
import chat.schildi.theme.scAvatarColorsDark
import chat.schildi.theme.scAvatarColorsLight
import io.element.android.compound.theme.AvatarColors
import io.element.android.compound.theme.avatarColorsDark
import io.element.android.compound.theme.avatarColorsLight

object AvatarColorsProvider {
    private val cache = LruCache<String, AvatarColors>(200)
    private var currentThemeIsLight = true
    private var currentThemeIsSc = true

    fun provide(id: String, isLightTheme: Boolean, isScTheme: Boolean): AvatarColors {
        if (currentThemeIsLight != isLightTheme || currentThemeIsSc != isScTheme) {
            currentThemeIsLight = isLightTheme
            currentThemeIsSc = isScTheme
            cache.evictAll()
        }
        val valueFromCache = cache.get(id)
        return if (valueFromCache != null) {
            valueFromCache
        } else {
            val colors = avatarColors(id, isLightTheme, isScTheme)
            cache.put(id, colors)
            colors
        }
    }

    private fun avatarColors(id: String, isLightTheme: Boolean, isScTheme: Boolean): AvatarColors {
        val hash = id.toHash()
        val colors = if (isLightTheme) {
            if (isScTheme) scAvatarColorsLight[hash] else avatarColorsLight[hash]
        } else {
            if (isScTheme) scAvatarColorsDark[hash] else avatarColorsDark[hash]
        }
        return colors
    }
}

internal fun String.toHash(): Int {
    return toList().sumOf { it.code } % avatarColorsLight.size
}
