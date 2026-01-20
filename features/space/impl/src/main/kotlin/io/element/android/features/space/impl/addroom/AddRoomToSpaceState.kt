/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl.addroom

import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.ui.model.SelectRoomInfo
import kotlinx.collections.immutable.ImmutableList

data class AddRoomToSpaceState(
    val searchQuery: String,
    val isSearchActive: Boolean,
    val searchResults: SearchBarResultState<ImmutableList<SelectRoomInfo>>,
    val selectedRooms: ImmutableList<SelectRoomInfo>,
    val suggestions: ImmutableList<SelectRoomInfo>,
    val saveAction: AsyncAction<Unit>,
    val eventSink: (AddRoomToSpaceEvents) -> Unit,
) {
    val canSave: Boolean = selectedRooms.isNotEmpty() && !saveAction.isLoading()
}
