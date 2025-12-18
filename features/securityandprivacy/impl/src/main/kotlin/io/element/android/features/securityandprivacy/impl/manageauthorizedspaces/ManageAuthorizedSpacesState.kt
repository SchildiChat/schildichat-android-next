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

data class ManageAuthorizedSpacesState(
    val joinedSpaces: ImmutableList<SpaceRoom>,
    val unknownSpaceIds: ImmutableList<RoomId>,
    val currentSelection: ImmutableList<RoomId>,
    val initialSelection: ImmutableList<RoomId>,
    val eventSink: (ManageAuthorizedSpacesEvent) -> Unit
)
