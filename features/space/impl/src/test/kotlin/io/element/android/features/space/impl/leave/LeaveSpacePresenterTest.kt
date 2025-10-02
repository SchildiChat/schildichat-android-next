/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalCoroutinesApi::class)

package io.element.android.features.space.impl.leave

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.spaces.SpaceRoomList
import io.element.android.libraries.matrix.test.A_SPACE_NAME
import io.element.android.libraries.matrix.test.spaces.FakeSpaceRoomList
import io.element.android.libraries.previewutils.room.aSpaceRoom
import io.element.android.tests.testutils.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

class LeaveSpacePresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val presenter = createLeaveSpacePresenter()
        presenter.test {
            val state = awaitItem()
            assertThat(state.spaceName).isNull()
            assertThat(state.selectableSpaceRooms).isEqualTo(AsyncData.Uninitialized)
            assertThat(state.leaveSpaceAction).isEqualTo(AsyncAction.Uninitialized)
            skipItems(1)
        }
    }

    @Test
    fun `present - current space name`() = runTest {
        val fakeSpaceRoomList = FakeSpaceRoomList()
        val presenter = createLeaveSpacePresenter(
            spaceRoomList = fakeSpaceRoomList,
        )
        presenter.test {
            val state = awaitItem()
            advanceUntilIdle()
            assertThat(state.spaceName).isNull()
            val aSpace = aSpaceRoom(
                name = A_SPACE_NAME
            )
            fakeSpaceRoomList.emitCurrentSpace(aSpace)
            skipItems(1)
            assertThat(awaitItem().spaceName).isEqualTo(A_SPACE_NAME)
        }
    }

    private fun createLeaveSpacePresenter(
        spaceRoomList: SpaceRoomList = FakeSpaceRoomList(),
    ): LeaveSpacePresenter {
        return LeaveSpacePresenter(
            spaceRoomList = spaceRoomList,
        )
    }
}
