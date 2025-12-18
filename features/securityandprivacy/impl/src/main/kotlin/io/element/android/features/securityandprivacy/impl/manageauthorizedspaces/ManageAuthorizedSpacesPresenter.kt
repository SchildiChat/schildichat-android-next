/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securityandprivacy.impl.manageauthorizedspaces

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.element.android.features.securityandprivacy.impl.SecurityAndPrivacyNavigator
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.room.JoinedRoom
import kotlinx.collections.immutable.persistentListOf

@AssistedInject
class EditRoomAddressPresenter(
    @Assisted private val navigator: SecurityAndPrivacyNavigator,
    private val client: MatrixClient,
    private val room: JoinedRoom,
) : Presenter<ManageAuthorizedSpacesState> {
    @AssistedFactory
    interface Factory {
        fun create(navigator: SecurityAndPrivacyNavigator): EditRoomAddressPresenter
    }

    @Composable
    override fun present(): ManageAuthorizedSpacesState {
        val roomInfo by room.roomInfoFlow.collectAsState()
        fun handleEvent(event: ManageAuthorizedSpacesEvent) {
            when (event) {
                ManageAuthorizedSpacesEvent.Done -> TODO()
                is ManageAuthorizedSpacesEvent.ToggleSpace -> TODO()
            }
        }

        return ManageAuthorizedSpacesState(
            joinedSpaces = persistentListOf(),
            unknownSpaceIds = persistentListOf(),
            currentSelection = persistentListOf(),
            initialSelection = persistentListOf(),
            eventSink = ::handleEvent,
        )
    }
}
