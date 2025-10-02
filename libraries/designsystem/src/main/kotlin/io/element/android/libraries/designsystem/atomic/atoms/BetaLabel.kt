/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.atomic.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.element.android.compound.annotations.CoreColorToken
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.internal.DarkColorTokens
import io.element.android.compound.tokens.generated.internal.LightColorTokens
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Text

@OptIn(CoreColorToken::class)
@Composable
fun BetaLabel(
    modifier: Modifier = Modifier,
) {
    val (backgroundColor, borderColor, textColor) = if (ElementTheme.isLightTheme) {
        listOf(
            LightColorTokens.colorGreen300,
            LightColorTokens.colorGreen700,
            LightColorTokens.colorGreen900,
        )
    } else {
        listOf(
            DarkColorTokens.colorGreen300,
            DarkColorTokens.colorGreen700,
            DarkColorTokens.colorGreen900,
        )
    }
    val shape = RoundedCornerShape(size = 6.dp)
    Text(
        modifier = modifier
            .border(
                width = 1.dp,
                color = borderColor,
                shape = shape,
            )
            .background(
                color = backgroundColor,
                shape = shape,
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        text = "BETA",
        style = ElementTheme.typography.fontBodySmMedium,
        color = textColor,
    )
}

@PreviewsDayNight
@Composable
internal fun BetaLabelPreview() = ElementPreview {
    BetaLabel()
}
