/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dev.zacsweers.metro.Inject
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.ui.room.isOwnUserAdmin

@Inject
class SpaceSettingsPresenter(
    private val room: JoinedRoom,
) : Presenter<SpaceSettingsState> {
    @Composable
    override fun present(): SpaceSettingsState {
        val roomInfo by room.roomInfoFlow.collectAsState()
        val isUserAdmin = room.isOwnUserAdmin()
        return SpaceSettingsState(
            roomId = room.roomId,
            name = roomInfo.name.orEmpty(),
            canonicalAlias = roomInfo.canonicalAlias,
            avatarUrl = roomInfo.avatarUrl,
            memberCount = roomInfo.activeMembersCount,
            showRolesAndPermissions = isUserAdmin,
            showSecurityAndPrivacy = isUserAdmin,
            eventSink = {},
        )
    }
}
