/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.avatar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.element.android.compound.theme.AvatarColors
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup
import io.element.android.libraries.designsystem.text.toSp
import io.element.android.libraries.designsystem.theme.components.Text

@Composable
internal fun TextAvatar(
    text: String,
    shape: Shape = CircleShape, // SC
    size: Dp,
    colors: AvatarColors,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .size(size)
            .clip(shape)
            .background(color = colors.background)
    ) {
        val fontSize = size.toSp() / 2
        val originalFont = ElementTheme.typography.fontHeadingMdBold
        val ratio = fontSize.value / originalFont.fontSize.value
        val lineHeight = originalFont.lineHeight * ratio
        Text(
            modifier = Modifier
                .clearAndSetSemantics {
                    contentDescription?.let {
                        this.contentDescription = it
                    }
                }
                .align(Alignment.Center),
            text = text,
            style = originalFont.copy(fontSize = fontSize, lineHeight = lineHeight, letterSpacing = 0.sp),
            color = colors.foreground,
        )
    }
}

@Preview(group = PreviewGroup.Avatars)
@Composable
internal fun TextAvatarPreview() = ElementPreview {
        TextAvatar(
            text = "AB",
            size = 40.dp,
            colors = AvatarColors(
                background = ElementTheme.colors.bgSubtlePrimary,
                foreground = ElementTheme.colors.iconPrimary,
            ),
            contentDescription = null,
        )
    }
