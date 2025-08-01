/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.avatar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.components.avatar.internal.InitialOrImageAvatar
import io.element.android.libraries.designsystem.components.avatar.internal.RoomAvatar
import io.element.android.libraries.designsystem.components.avatar.internal.SpaceAvatar
import io.element.android.libraries.designsystem.components.avatar.internal.UserAvatar
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.utils.CommonDrawables

@Composable
fun Avatar(
    avatarData: AvatarData,
    avatarType: AvatarType,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    // If not null, will be used instead of the size from avatarData
    forcedAvatarSize: Dp? = null,
    // If true, will show initials even if avatarData.url is not null
    hideImage: Boolean = false,
) {
    when (avatarType) {
        is AvatarType.Sc -> InitialOrImageAvatar(
            avatarData = avatarData,
            hideAvatarImage = hideImage,
            avatarShape = avatarType.avatarShape(forcedAvatarSize ?: avatarData.size.dp),
            forcedAvatarSize = forcedAvatarSize,
            modifier = modifier,
            contentDescription = contentDescription,
        )
        is AvatarType.Room -> RoomAvatar(
            avatarData = avatarData,
            avatarType = avatarType,
            modifier = modifier,
            hideAvatarImage = hideImage,
            forcedAvatarSize = forcedAvatarSize,
            contentDescription = contentDescription,
        )
        AvatarType.User -> UserAvatar(
            avatarData = avatarData,
            modifier = modifier,
            contentDescription = contentDescription,
            forcedAvatarSize = forcedAvatarSize,
            hideImage = hideImage,
        )
        is AvatarType.Space -> SpaceAvatar(
            avatarData = avatarData,
            avatarType = avatarType,
            modifier = modifier,
            hideAvatarImage = hideImage,
            forcedAvatarSize = forcedAvatarSize,
            contentDescription = contentDescription,
        )
    }
}

@Preview(group = PreviewGroup.Avatars)
@Composable
internal fun AvatarPreview(@PreviewParameter(AvatarDataProvider::class) avatarData: AvatarData) =
    ElementThemedPreview(
        drawableFallbackForImages = CommonDrawables.sample_avatar,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Avatar(
                avatarData = avatarData,
                avatarType = AvatarType.User,
            )
            Text(text = avatarData.size.name + " " + avatarData.size.dp)
        }
    }
