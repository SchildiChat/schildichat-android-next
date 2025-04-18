/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.createroom.impl

import android.net.Uri
import io.element.android.features.createroom.impl.configureroom.RoomVisibilityState
import io.element.android.libraries.matrix.api.user.MatrixUser
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class CreateRoomConfig(
    val roomName: String? = null,
    val topic: String? = null,
    val avatarUri: Uri? = null,
    val invites: ImmutableList<MatrixUser> = persistentListOf(),
    val roomVisibility: RoomVisibilityState = RoomVisibilityState.Private,
)
