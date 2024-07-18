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

package io.element.android.libraries.designsystem.theme

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import chat.schildi.lib.preferences.ScPrefs
import chat.schildi.lib.preferences.value
import io.element.android.compound.annotations.CoreColorToken
import io.element.android.compound.previews.ColorListPreview
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.SemanticColors
import io.element.android.compound.tokens.generated.internal.DarkColorTokens
import io.element.android.compound.tokens.generated.internal.LightColorTokens
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import kotlinx.collections.immutable.persistentMapOf

object DynamicColorPreferences {
    val isDynamicColorEnabled: Boolean
        @Composable
        get() = ScPrefs.SC_DYNAMICCOLORS.value()
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun getDynamicColorScheme(isDark: Boolean): ColorScheme {
    val context = LocalContext.current
    return if (DynamicColorPreferences.isDynamicColorEnabled) {
        if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        if (isDark) darkColorScheme() else lightColorScheme()
    }
}
/**
 * Room list.
 */
@Composable
fun MaterialTheme.roomListRoomName() = colorScheme.primary

@Composable
fun MaterialTheme.roomListRoomMessage() = colorScheme.secondary

@Composable
fun MaterialTheme.roomListRoomMessageDate() = colorScheme.secondary

val SemanticColors.unreadIndicator
    get() = iconAccentTertiary

val SemanticColors.placeholderBackground
    get() = bgSubtleSecondary

val SemanticColors.messageFromMeBackground
    @Composable
    get() = if (DynamicColorPreferences.isDynamicColorEnabled) {
        colorScheme.primaryContainer
    } else if (isLight) {
        Color(0xFFE1E6EC)
    } else {
        Color(0xFF323539)
    }


val SemanticColors.messageFromOtherBackground
    @Composable
    get() = if (DynamicColorPreferences.isDynamicColorEnabled) {
        colorScheme.secondaryContainer
    } else if (isLight) {
        Color(0xFFF0F2F5)
    } else {
        Color(0xFF26282D)
    }

val SemanticColors.progressIndicatorTrackColor
    @Composable
    get() = if (DynamicColorPreferences.isDynamicColorEnabled) {
        colorScheme.onSurfaceVariant
    } else if (isLight) {
        Color(0x33052448)
    } else {
        Color(0x25F4F7FA)
    }

val SemanticColors.iconSuccessPrimaryBackground
    @Composable
    get() = if (DynamicColorPreferences.isDynamicColorEnabled) {
        colorScheme.tertiaryContainer
    } else if (isLight) {
        Color(0xffe3f7ed)
    } else {
        Color(0xff002513)
    }

val SemanticColors.bgSubtleTertiary
    @Composable
    get() = if (DynamicColorPreferences.isDynamicColorEnabled) {
        colorScheme.surfaceVariant
    } else if (isLight) {
        Color(0xfffbfcfd)
    } else {
        Color(0xff14171b)
    }

val SemanticColors.temporaryColorBgSpecial
    @Composable
    get() = if (DynamicColorPreferences.isDynamicColorEnabled) {
        colorScheme.background
    } else if (isLight) Color(0xFFE4E8F0) else Color(0xFF3A4048)

val SemanticColors.pinDigitBg
    @Composable
    get() = if (DynamicColorPreferences.isDynamicColorEnabled) {
        colorScheme.onPrimaryContainer
    } else if (isLight) {
        Color(0xFFF0F2F5)
    } else {
        Color(0xFF26282D)
    }

val SemanticColors.currentUserMentionPillText
    @Composable
    get() = if (DynamicColorPreferences.isDynamicColorEnabled) {
        colorScheme.onTertiaryContainer
    } else if (isLight) {
        Color(0xff005c45)
    } else {
        Color(0xff1fc090)
    }

val SemanticColors.currentUserMentionPillBackground
    @Composable
    get() = if (DynamicColorPreferences.isDynamicColorEnabled) {
        colorScheme.tertiary
    } else if (isLight) {
        Color(0x3b07b661)
    } else {
        Color(0xff003d29)
    }

val SemanticColors.mentionPillText
    @Composable
    get() = if (DynamicColorPreferences.isDynamicColorEnabled) {
        colorScheme.onSecondaryContainer
    } else textPrimary

val SemanticColors.mentionPillBackground
    @Composable
    get() = if (DynamicColorPreferences.isDynamicColorEnabled) {
        colorScheme.secondary
    } else if (isLight) {
        Color(0x1f052e61)
    } else {
        Color(0x26f4f7fa)
    }

@OptIn(CoreColorToken::class)
val SemanticColors.bigIconDefaultBackgroundColor
    @Composable
    get() = if (DynamicColorPreferences.isDynamicColorEnabled) {
        colorScheme.onSurface
    } else if (isLight) LightColorTokens.colorAlphaGray300 else DarkColorTokens.colorAlphaGray300

@OptIn(CoreColorToken::class)
val SemanticColors.bigCheckmarkBorderColor
    @Composable
    get() = if (DynamicColorPreferences.isDynamicColorEnabled) {
        colorScheme.outline
    } else if (isLight) LightColorTokens.colorGray400 else DarkColorTokens.colorGray400

@OptIn(CoreColorToken::class)
val SemanticColors.highlightedMessageBackgroundColor
    @Composable
    get() = if (DynamicColorPreferences.isDynamicColorEnabled) {
        colorScheme.primary
    } else if (isLight) LightColorTokens.colorGreen300 else DarkColorTokens.colorGreen300

// Badge colors

@OptIn(CoreColorToken::class)
val SemanticColors.badgePositiveBackgroundColor
    @Composable
    get() = if (DynamicColorPreferences.isDynamicColorEnabled) {
        colorScheme.primaryContainer
    } else if (isLight) LightColorTokens.colorAlphaGreen300 else DarkColorTokens.colorAlphaGreen300

@OptIn(CoreColorToken::class)
val SemanticColors.badgePositiveContentColor
    @Composable
    get() = if (DynamicColorPreferences.isDynamicColorEnabled) {
        colorScheme.onPrimaryContainer
    } else if (isLight) LightColorTokens.colorGreen1100 else DarkColorTokens.colorGreen1100

@OptIn(CoreColorToken::class)
val SemanticColors.badgeNeutralBackgroundColor
    @Composable
    get() = if (DynamicColorPreferences.isDynamicColorEnabled) {
        colorScheme.surfaceVariant
    } else if (isLight) LightColorTokens.colorAlphaGray300 else DarkColorTokens.colorAlphaGray300

@OptIn(CoreColorToken::class)
val SemanticColors.badgeNeutralContentColor
    @Composable
    get() = if (DynamicColorPreferences.isDynamicColorEnabled) {
        colorScheme.onSurfaceVariant
    } else if (isLight) LightColorTokens.colorGray1100 else DarkColorTokens.colorGray1100

@OptIn(CoreColorToken::class)
val SemanticColors.badgeNegativeBackgroundColor
    @Composable
    get() = if (DynamicColorPreferences.isDynamicColorEnabled) {
        colorScheme.errorContainer
    } else if (isLight) LightColorTokens.colorAlphaRed300 else DarkColorTokens.colorAlphaRed300

@OptIn(CoreColorToken::class)
val SemanticColors.badgeNegativeContentColor
    @Composable
    get() = if (DynamicColorPreferences.isDynamicColorEnabled) {
        colorScheme.onErrorContainer
    } else if (isLight) LightColorTokens.colorRed1100 else DarkColorTokens.colorRed1100
@PreviewsDayNight
@Composable
internal fun ColorAliasesPreview() = ElementPreview {
    ColorListPreview(
        backgroundColor = Color.Black,
        foregroundColor = Color.White,
        colors = persistentMapOf(
            "roomListRoomName" to MaterialTheme.roomListRoomName(),
            "roomListRoomMessage" to MaterialTheme.roomListRoomMessage(),
            "roomListRoomMessageDate" to MaterialTheme.roomListRoomMessageDate(),
            "unreadIndicator" to ElementTheme.colors.unreadIndicator,
            "placeholderBackground" to ElementTheme.colors.placeholderBackground,
            "messageFromMeBackground" to ElementTheme.colors.messageFromMeBackground,
            "messageFromOtherBackground" to ElementTheme.colors.messageFromOtherBackground,
            "progressIndicatorTrackColor" to ElementTheme.colors.progressIndicatorTrackColor,
            "temporaryColorBgSpecial" to ElementTheme.colors.temporaryColorBgSpecial,
            "iconSuccessPrimaryBackground" to ElementTheme.colors.iconSuccessPrimaryBackground,
            "bigIconBackgroundColor" to ElementTheme.colors.bigIconDefaultBackgroundColor,
            "bigCheckmarkBorderColor" to ElementTheme.colors.bigCheckmarkBorderColor,
            "highlightedMessageBackgroundColor" to ElementTheme.colors.highlightedMessageBackgroundColor,
        )
    )
}
