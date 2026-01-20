/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl.addroom

import io.element.android.libraries.matrix.ui.model.SelectRoomInfo

sealed interface AddRoomToSpaceEvents {
    data class ToggleRoom(val room: SelectRoomInfo) : AddRoomToSpaceEvents
    data class UpdateSearchQuery(val query: String) : AddRoomToSpaceEvents
    data class OnSearchActiveChanged(val active: Boolean) : AddRoomToSpaceEvents
    data object Save : AddRoomToSpaceEvents
    data object CloseSearch : AddRoomToSpaceEvents
    data object ClearError : AddRoomToSpaceEvents
}
