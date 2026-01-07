/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securityandprivacy.impl.manageauthorizedspaces

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.previewutils.room.aSpaceRoom
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.pressBack
import kotlinx.collections.immutable.toImmutableList
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ManageAuthorizedSpacesViewTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `clicking back invokes callback`() {
        ensureCalledOnce { callback ->
            rule.setManageAuthorizedSpacesView(onBackClick = callback)
            rule.pressBack()
        }
    }

    @Test
    fun `clicking space checkbox emits ToggleSpace event`() {
        val roomId = A_ROOM_ID
        val space = aSpaceRoom(roomId = roomId, displayName = "Test Space")
        val recorder = EventsRecorder<ManageAuthorizedSpacesEvent>()
        val state = aManageAuthorizedSpacesState(
            selection = anAuthorizedSpaceSelection(
                joinedSpaces = listOf(space)
            ),
            eventSink = recorder
        )
        rule.setManageAuthorizedSpacesView(state)
        rule.onNodeWithText("Test Space").performClick()
        recorder.assertSingle(ManageAuthorizedSpacesEvent.ToggleSpace(roomId))
    }

    @Test
    fun `clicking done button emits Done event`() {
        val recorder = EventsRecorder<ManageAuthorizedSpacesEvent>()
        val state = aManageAuthorizedSpacesState(
            selectedIds = listOf(A_ROOM_ID),
            eventSink = recorder
        )
        rule.setManageAuthorizedSpacesView(state)
        rule.clickOn(CommonStrings.action_done)
        recorder.assertSingle(ManageAuthorizedSpacesEvent.Done)
    }

    @Test
    fun `done button is disabled when no spaces selected`() {
        val recorder = EventsRecorder<ManageAuthorizedSpacesEvent>(expectEvents = false)
        val state = aManageAuthorizedSpacesState(
            selectedIds = emptyList(),
            eventSink = recorder
        )
        rule.setManageAuthorizedSpacesView(state)
        rule.clickOn(CommonStrings.action_done)
        recorder.assertEmpty()
    }
}

private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setManageAuthorizedSpacesView(
    state: ManageAuthorizedSpacesState = aManageAuthorizedSpacesState(
        eventSink = EventsRecorder(expectEvents = false)
    ),
    onBackClick: () -> Unit = EnsureNeverCalled(),
) {
    setContent {
        ManageAuthorizedSpacesView(
            state = state,
            onBackClick = onBackClick,
        )
    }
}

private fun aManageAuthorizedSpacesState(
    selection: AuthorizedSpacesSelection = AuthorizedSpacesSelection(),
    selectedIds: List<RoomId> = emptyList(),
    isSelectionComplete: Boolean = false,
    eventSink: (ManageAuthorizedSpacesEvent) -> Unit = {},
) = ManageAuthorizedSpacesState(
    selection = selection,
    selectedIds = selectedIds.toImmutableList(),
    isSelectionComplete = isSelectionComplete,
    eventSink = eventSink,
)
