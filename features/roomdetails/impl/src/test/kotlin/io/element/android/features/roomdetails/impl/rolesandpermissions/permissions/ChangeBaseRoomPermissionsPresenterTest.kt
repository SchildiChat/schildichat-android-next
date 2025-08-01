/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.rolesandpermissions.permissions

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.Event
import app.cash.turbine.TurbineTestContext
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import im.vector.app.features.analytics.plan.RoomModeration
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.room.RoomMember.Role.Admin
import io.element.android.libraries.matrix.api.room.RoomMember.Role.Moderator
import io.element.android.libraries.matrix.api.room.RoomMember.Role.User
import io.element.android.libraries.matrix.api.room.powerlevels.RoomPowerLevelsValues
import io.element.android.libraries.matrix.test.room.FakeBaseRoom
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.matrix.test.room.defaultRoomPowerLevelValues
import io.element.android.services.analytics.test.FakeAnalyticsService
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ChangeBaseRoomPermissionsPresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val section = ChangeRoomPermissionsSection.RoomDetails
        val presenter = createChangeRoomPermissionsPresenter(section = section)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            // Initial state, no permissions loaded
            awaitItem().run {
                assertThat(this.section).isEqualTo(section)
                assertThat(this.currentPermissions).isNull()
                assertThat(this.items).isNotEmpty()
                assertThat(this.hasChanges).isFalse()
                assertThat(this.saveAction).isEqualTo(AsyncAction.Uninitialized)
                assertThat(this.confirmExitAction).isEqualTo(AsyncAction.Uninitialized)
            }

            // Updated state, permissions loaded
            assertThat(awaitItem().currentPermissions).isEqualTo(defaultPermissions())
        }
    }

    @Test
    fun `present - RoomDetails section contains the right items`() = runTest {
        val section = ChangeRoomPermissionsSection.RoomDetails
        val presenter = createChangeRoomPermissionsPresenter(section = section)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            assertThat(awaitUpdatedItem().items).containsExactly(
                RoomPermissionType.ROOM_NAME,
                RoomPermissionType.ROOM_AVATAR,
                RoomPermissionType.ROOM_TOPIC,
            )
        }
    }

    @Test
    fun `present - MessagesAndContent section contains the right items`() = runTest {
        val section = ChangeRoomPermissionsSection.MessagesAndContent
        val presenter = createChangeRoomPermissionsPresenter(section = section)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            assertThat(awaitUpdatedItem().items).containsExactly(
                RoomPermissionType.SEND_EVENTS,
                RoomPermissionType.REDACT_EVENTS,
            )
        }
    }

    @Test
    fun `present - MembershipModeration section contains the right items`() = runTest {
        val section = ChangeRoomPermissionsSection.MembershipModeration
        val presenter = createChangeRoomPermissionsPresenter(section = section)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            assertThat(awaitUpdatedItem().items).containsExactly(
                RoomPermissionType.INVITE,
                RoomPermissionType.KICK,
                RoomPermissionType.BAN,
            )
        }
    }

    @Test
    fun `present - ChangeMinimumRoleForAction updates the current permissions and hasChanges`() = runTest {
        val presenter = createChangeRoomPermissionsPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val state = awaitUpdatedItem()
            assertThat(state.currentPermissions?.roomName).isEqualTo(Admin.powerLevel)
            assertThat(state.hasChanges).isFalse()

            state.eventSink(ChangeRoomPermissionsEvent.ChangeMinimumRoleForAction(RoomPermissionType.ROOM_NAME, Moderator))

            awaitItem().run {
                assertThat(currentPermissions?.roomName).isEqualTo(Moderator.powerLevel)
                assertThat(hasChanges).isTrue()
            }
        }
    }

    @Test
    fun `present - ChangeMinimumRoleForAction works for all actions`() = runTest {
        val presenter = createChangeRoomPermissionsPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val state = awaitUpdatedItem()

            state.eventSink(ChangeRoomPermissionsEvent.ChangeMinimumRoleForAction(RoomPermissionType.INVITE, Moderator))
            state.eventSink(ChangeRoomPermissionsEvent.ChangeMinimumRoleForAction(RoomPermissionType.KICK, Moderator))
            state.eventSink(ChangeRoomPermissionsEvent.ChangeMinimumRoleForAction(RoomPermissionType.BAN, Moderator))
            state.eventSink(ChangeRoomPermissionsEvent.ChangeMinimumRoleForAction(RoomPermissionType.SEND_EVENTS, Moderator))
            state.eventSink(ChangeRoomPermissionsEvent.ChangeMinimumRoleForAction(RoomPermissionType.REDACT_EVENTS, Moderator))
            state.eventSink(ChangeRoomPermissionsEvent.ChangeMinimumRoleForAction(RoomPermissionType.ROOM_NAME, Moderator))
            state.eventSink(ChangeRoomPermissionsEvent.ChangeMinimumRoleForAction(RoomPermissionType.ROOM_AVATAR, Moderator))
            state.eventSink(ChangeRoomPermissionsEvent.ChangeMinimumRoleForAction(RoomPermissionType.ROOM_TOPIC, Moderator))

            val items = cancelAndConsumeRemainingEvents()

            (items.last() as? Event.Item<ChangeRoomPermissionsState>)?.value?.run {
                assertThat(currentPermissions).isEqualTo(
                    RoomPowerLevelsValues(
                        invite = Moderator.powerLevel,
                        kick = Moderator.powerLevel,
                        ban = Moderator.powerLevel,
                        redactEvents = Moderator.powerLevel,
                        sendEvents = Moderator.powerLevel,
                        roomName = Moderator.powerLevel,
                        roomAvatar = Moderator.powerLevel,
                        roomTopic = Moderator.powerLevel,
                    )
                )
            }
        }
    }

    @Test
    fun `present - Save updates the current permissions and resets hasChanges`() = runTest {
        val analyticsService = FakeAnalyticsService()
        val presenter = createChangeRoomPermissionsPresenter(
            analyticsService = analyticsService,
            room = FakeJoinedRoom(
                updatePowerLevelsResult = { Result.success(Unit) },
                baseRoom = FakeBaseRoom(powerLevelsResult = { Result.success(defaultPermissions()) }),
            ),
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val state = awaitUpdatedItem()
            assertThat(state.currentPermissions?.roomName).isEqualTo(Admin.powerLevel)
            assertThat(state.hasChanges).isFalse()

            state.eventSink(ChangeRoomPermissionsEvent.ChangeMinimumRoleForAction(RoomPermissionType.ROOM_NAME, Moderator))
            state.eventSink(ChangeRoomPermissionsEvent.ChangeMinimumRoleForAction(RoomPermissionType.ROOM_AVATAR, Moderator))
            state.eventSink(ChangeRoomPermissionsEvent.ChangeMinimumRoleForAction(RoomPermissionType.ROOM_TOPIC, Moderator))
            state.eventSink(ChangeRoomPermissionsEvent.ChangeMinimumRoleForAction(RoomPermissionType.SEND_EVENTS, Moderator))
            state.eventSink(ChangeRoomPermissionsEvent.ChangeMinimumRoleForAction(RoomPermissionType.REDACT_EVENTS, User))
            state.eventSink(ChangeRoomPermissionsEvent.ChangeMinimumRoleForAction(RoomPermissionType.KICK, Admin))
            state.eventSink(ChangeRoomPermissionsEvent.ChangeMinimumRoleForAction(RoomPermissionType.BAN, Admin))
            state.eventSink(ChangeRoomPermissionsEvent.ChangeMinimumRoleForAction(RoomPermissionType.INVITE, Admin))
            skipItems(7)
            assertThat(awaitItem().hasChanges).isTrue()

            state.eventSink(ChangeRoomPermissionsEvent.Save)

            assertThat(awaitItem().saveAction).isEqualTo(AsyncAction.Loading)
            assertThat(awaitItem().hasChanges).isFalse()
            awaitItem().run {
                assertThat(currentPermissions?.roomName).isEqualTo(Moderator.powerLevel)
                assertThat(saveAction).isEqualTo(AsyncAction.Success(Unit))
            }
            assertThat(analyticsService.capturedEvents).containsExactlyElementsIn(
                listOf(
                    RoomModeration(RoomModeration.Action.ChangePermissionsRoomName, RoomModeration.Role.Moderator),
                    RoomModeration(RoomModeration.Action.ChangePermissionsRoomAvatar, RoomModeration.Role.Moderator),
                    RoomModeration(RoomModeration.Action.ChangePermissionsRoomTopic, RoomModeration.Role.Moderator),
                    RoomModeration(RoomModeration.Action.ChangePermissionsSendMessages, RoomModeration.Role.Moderator),
                    RoomModeration(RoomModeration.Action.ChangePermissionsRedactMessages, RoomModeration.Role.User),
                    RoomModeration(RoomModeration.Action.ChangePermissionsKickMembers, RoomModeration.Role.Administrator),
                    RoomModeration(RoomModeration.Action.ChangePermissionsBanMembers, RoomModeration.Role.Administrator),
                    RoomModeration(RoomModeration.Action.ChangePermissionsInviteUsers, RoomModeration.Role.Administrator),
                )
            )
        }
    }

    @Test
    fun `present - Save will fail if there are not current permissions`() = runTest {
        val room = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(powerLevelsResult = { Result.failure(IllegalStateException("Failed to load power levels")) }),
        )
        val presenter = createChangeRoomPermissionsPresenter(room = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val state = awaitItem()
            assertThat(state.currentPermissions).isNull()

            state.eventSink(ChangeRoomPermissionsEvent.Save)
            assertThat(awaitItem().saveAction).isInstanceOf(AsyncAction.Failure::class.java)
        }
    }

    @Test
    fun `present - Save can handle failures and they can be cleared`() = runTest {
        val room = FakeJoinedRoom(
            updatePowerLevelsResult = { Result.failure(IllegalStateException("Failed to update power levels")) },
            baseRoom = FakeBaseRoom(powerLevelsResult = { Result.success(defaultPermissions()) }),
        )
        val presenter = createChangeRoomPermissionsPresenter(room = room)
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val state = awaitUpdatedItem()
            assertThat(state.currentPermissions?.roomName).isEqualTo(Admin.powerLevel)
            assertThat(state.hasChanges).isFalse()

            state.eventSink(ChangeRoomPermissionsEvent.ChangeMinimumRoleForAction(RoomPermissionType.ROOM_NAME, Moderator))
            assertThat(awaitItem().hasChanges).isTrue()

            state.eventSink(ChangeRoomPermissionsEvent.Save)

            assertThat(awaitItem().saveAction).isEqualTo(AsyncAction.Loading)
            awaitItem().run {
                assertThat(currentPermissions?.roomName).isEqualTo(Moderator.powerLevel)
                // Couldn't save the changes, so they're still pending
                assertThat(hasChanges).isTrue()
                assertThat(saveAction).isInstanceOf(AsyncAction.Failure::class.java)
            }

            state.eventSink(ChangeRoomPermissionsEvent.ResetPendingActions)
            awaitItem().run {
                assertThat(currentPermissions?.roomName).isEqualTo(Moderator.powerLevel)
                assertThat(saveAction).isEqualTo(AsyncAction.Uninitialized)
                assertThat(hasChanges).isTrue()
            }
        }
    }

    @Test
    fun `present - Exit does not need a confirmation when there are no pending changes`() = runTest {
        val presenter = createChangeRoomPermissionsPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val state = awaitUpdatedItem()
            state.eventSink(ChangeRoomPermissionsEvent.ChangeMinimumRoleForAction(RoomPermissionType.ROOM_NAME, Moderator))
            assertThat(awaitItem().hasChanges).isTrue()

            state.eventSink(ChangeRoomPermissionsEvent.Exit)
            assertThat(awaitItem().confirmExitAction).isEqualTo(AsyncAction.ConfirmingNoParams)

            state.eventSink(ChangeRoomPermissionsEvent.Exit)
            assertThat(awaitItem().confirmExitAction).isEqualTo(AsyncAction.Success(Unit))
        }
    }

    @Test
    fun `present - Exit needs confirmation when there are pending changes`() = runTest {
        val presenter = createChangeRoomPermissionsPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val state = awaitUpdatedItem()

            state.eventSink(ChangeRoomPermissionsEvent.Exit)

            assertThat(awaitItem().confirmExitAction).isEqualTo(AsyncAction.Success(Unit))
        }
    }

    private fun createChangeRoomPermissionsPresenter(
        section: ChangeRoomPermissionsSection = ChangeRoomPermissionsSection.RoomDetails,
        room: FakeJoinedRoom = FakeJoinedRoom(
            baseRoom = FakeBaseRoom(powerLevelsResult = { Result.success(defaultPermissions()) }),
        ),
        analyticsService: FakeAnalyticsService = FakeAnalyticsService(),
    ) = ChangeRoomPermissionsPresenter(
        section = section,
        room = room,
        analyticsService = analyticsService,
    )

    private fun defaultPermissions() = defaultRoomPowerLevelValues()

    private suspend fun TurbineTestContext<ChangeRoomPermissionsState>.awaitUpdatedItem(): ChangeRoomPermissionsState {
        skipItems(1)
        return awaitItem()
    }
}
