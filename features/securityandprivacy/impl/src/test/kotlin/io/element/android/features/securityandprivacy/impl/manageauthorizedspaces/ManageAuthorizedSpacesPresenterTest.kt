/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securityandprivacy.impl.manageauthorizedspaces

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.tests.testutils.test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ManageAuthorizedSpacesPresenterTest {
    @Test
    fun `present - initial state has empty selection`() = runTest {
        val presenter = ManageAuthorizedSpacesPresenter()
        presenter.test {
            with(awaitItem()) {
                assertThat(selectedIds).isEmpty()
                assertThat(isSelectionComplete).isFalse()
                assertThat(isDoneButtonEnabled).isFalse()
            }
        }
    }

    @Test
    fun `present - SetData event updates selection and initial selectedIds`() = runTest {
        val presenter = ManageAuthorizedSpacesPresenter()
        presenter.test {
            val initialState = awaitItem()
            val roomId = A_ROOM_ID
            val data = AuthorizedSpacesSelection(
                joinedSpaces = persistentListOf(),
                unknownSpaceIds = persistentListOf(),
                initialSelectedIds = persistentListOf(roomId)
            )
            initialState.eventSink(ManageAuthorizedSpacesEvent.SetData(data))
            // SetData updates two state variables, which may emit intermediate states
            skipItems(1)
            with(awaitItem()) {
                assertThat(selection).isEqualTo(data)
                assertThat(selectedIds).containsExactly(roomId)
                assertThat(isDoneButtonEnabled).isTrue()
            }
        }
    }

    @Test
    fun `present - ToggleSpace event adds space to selectedIds`() = runTest {
        val presenter = ManageAuthorizedSpacesPresenter()
        presenter.test {
            val initialState = awaitItem()
            val roomId = A_ROOM_ID
            initialState.eventSink(ManageAuthorizedSpacesEvent.ToggleSpace(roomId))
            with(awaitItem()) {
                assertThat(selectedIds).containsExactly(roomId)
                assertThat(isDoneButtonEnabled).isTrue()
            }
        }
    }

    @Test
    fun `present - ToggleSpace event removes space when already selected`() = runTest {
        val presenter = ManageAuthorizedSpacesPresenter()
        presenter.test {
            val initialState = awaitItem()
            val roomId = A_ROOM_ID
            initialState.eventSink(ManageAuthorizedSpacesEvent.ToggleSpace(roomId))
            val stateWithSelection = awaitItem()
            assertThat(stateWithSelection.selectedIds).containsExactly(roomId)
            stateWithSelection.eventSink(ManageAuthorizedSpacesEvent.ToggleSpace(roomId))
            with(awaitItem()) {
                assertThat(selectedIds).isEmpty()
                assertThat(isDoneButtonEnabled).isFalse()
            }
        }
    }

    @Test
    fun `present - Done event sets isSelectionComplete to true`() = runTest {
        val presenter = ManageAuthorizedSpacesPresenter()
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(ManageAuthorizedSpacesEvent.Done)
            with(awaitItem()) {
                assertThat(isSelectionComplete).isTrue()
            }
        }
    }
}
