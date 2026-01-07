/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securityandprivacy.impl.manageauthorizedspaces

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Inject
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.core.RoomId
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Inject
class ManageAuthorizedSpacesPresenter : Presenter<ManageAuthorizedSpacesState> {
    @Composable
    override fun present(): ManageAuthorizedSpacesState {
        var selectedIds: ImmutableList<RoomId> by remember { mutableStateOf(persistentListOf()) }
        var spacesSelection by remember { mutableStateOf(AuthorizedSpacesSelection()) }
        var isSelectionComplete by remember { mutableStateOf(false) }

        fun handleEvent(event: ManageAuthorizedSpacesEvent) {
            when (event) {
                ManageAuthorizedSpacesEvent.Done -> isSelectionComplete = true
                is ManageAuthorizedSpacesEvent.ToggleSpace -> {
                    selectedIds = if (selectedIds.contains(event.roomId)) {
                        selectedIds.minus(event.roomId).toImmutableList()
                    } else {
                        selectedIds.plus(event.roomId).toImmutableList()
                    }
                }
                is ManageAuthorizedSpacesEvent.SetData -> {
                    spacesSelection = event.data
                    selectedIds = event.data.initialSelectedIds
                }
            }
        }

        return ManageAuthorizedSpacesState(
            selection = spacesSelection,
            selectedIds = selectedIds,
            isSelectionComplete = isSelectionComplete,
            eventSink = ::handleEvent,
        )
    }
}
