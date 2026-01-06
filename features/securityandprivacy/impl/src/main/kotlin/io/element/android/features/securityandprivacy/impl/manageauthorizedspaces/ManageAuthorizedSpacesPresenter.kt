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
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.element.android.features.securityandprivacy.impl.SecurityAndPrivacyNavigator
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.JoinedRoom
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

@AssistedInject
class ManageAuthorizedSpacesPresenter(
    @Assisted private val navigator: SecurityAndPrivacyNavigator,
    private val client: MatrixClient,
    private val room: JoinedRoom,
) : Presenter<ManageAuthorizedSpacesState> {
    @AssistedFactory
    interface Factory {
        fun create(navigator: SecurityAndPrivacyNavigator): ManageAuthorizedSpacesPresenter
    }

    @Composable
    override fun present(): ManageAuthorizedSpacesState {
        var currentSelection: ImmutableList<RoomId> by remember { mutableStateOf(persistentListOf()) }
        var spacesData by remember { mutableStateOf(AuthorizedSpacesSelection()) }
        var isSelectionComplete by remember { mutableStateOf(false) }

        fun handleEvent(event: ManageAuthorizedSpacesEvent) {
            when (event) {
                ManageAuthorizedSpacesEvent.Done -> {
                    isSelectionComplete = true
                }
                is ManageAuthorizedSpacesEvent.ToggleSpace -> {
                    currentSelection = if (currentSelection.contains(event.roomId)) {
                        currentSelection.minus(event.roomId).toPersistentList()
                    } else {
                        currentSelection.plus(event.roomId).toPersistentList()
                    }
                }
                is ManageAuthorizedSpacesEvent.SetData -> {
                    spacesData = event.data
                    currentSelection = event.data.initialSelectedIds
                }
            }
        }

        return ManageAuthorizedSpacesState(
            selection = spacesData,
            selectedIds = currentSelection,
            isSelectionComplete = isSelectionComplete,
            eventSink = ::handleEvent,
        )
    }
}
