/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securityandprivacy.impl.manageauthorizedspaces

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class ManageAuthorizedSpacesState(
    val selection: AuthorizedSpacesSelection,
    val selectedIds: ImmutableList<RoomId>,
    val isSelectionComplete: Boolean,
    val eventSink: (ManageAuthorizedSpacesEvent) -> Unit
) {
    val isDoneButtonEnabled = selectedIds.isNotEmpty()
}

data class AuthorizedSpacesSelection(
    val joinedSpaces: ImmutableList<SpaceRoom> = persistentListOf(),
    val unknownSpaceIds: ImmutableList<RoomId> = persistentListOf(),
    val initialSelectedIds: ImmutableList<RoomId> = persistentListOf()
)
