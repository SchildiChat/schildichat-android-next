/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.rolesandpermissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import im.vector.app.features.analytics.plan.RoomModeration
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runUpdatingState
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.RoomInfo
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.activeRoomMembers
import io.element.android.libraries.matrix.api.room.powerlevels.UserRoleChange
import io.element.android.libraries.matrix.ui.model.roleOf
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class RolesAndPermissionsPresenter @Inject constructor(
    private val room: JoinedRoom,
    private val dispatchers: CoroutineDispatchers,
    private val analyticsService: AnalyticsService,
) : Presenter<RolesAndPermissionsState> {
    @Composable
    override fun present(): RolesAndPermissionsState {
        val coroutineScope = rememberCoroutineScope()
        val roomInfo by room.roomInfoFlow.collectAsState()
        val roomMembers by room.membersStateFlow.collectAsState()
        // Get the list of active room members (joined or invited), in order to filter members present in the power
        // level state Event.
        val activeRoomMemberIds by remember {
            derivedStateOf {
                roomMembers.activeRoomMembers().map { it.userId }
            }
        }
        val moderatorCount by remember {
            derivedStateOf {
                roomInfo.userCountWithRole(activeRoomMemberIds, RoomMember.Role.Moderator)
            }
        }
        val adminCount by remember {
            derivedStateOf {
                val admins = roomInfo.userCountWithRole(activeRoomMemberIds, RoomMember.Role.Admin)
                val ownersCount = if (roomInfo.privilegedCreatorRole) {
                    val superAdmins = roomInfo.userCountWithRole(activeRoomMemberIds, RoomMember.Role.Owner(isCreator = false))
                    val creators = roomInfo.userCountWithRole(activeRoomMemberIds, RoomMember.Role.Owner(isCreator = true))
                    superAdmins + creators
                } else {
                    0
                }
                admins + ownersCount
            }
        }
        val canDemoteSelf = remember { derivedStateOf { roomInfo.roleOf(room.sessionId) !is RoomMember.Role.Owner } }
        val changeOwnRoleAction = remember { mutableStateOf<AsyncAction<Unit>>(AsyncAction.Uninitialized) }
        val resetPermissionsAction = remember { mutableStateOf<AsyncAction<Unit>>(AsyncAction.Uninitialized) }

        fun handleEvent(event: RolesAndPermissionsEvents) {
            when (event) {
                is RolesAndPermissionsEvents.ChangeOwnRole -> {
                    changeOwnRoleAction.value = AsyncAction.ConfirmingNoParams
                }
                is RolesAndPermissionsEvents.CancelPendingAction -> {
                    changeOwnRoleAction.value = AsyncAction.Uninitialized
                    resetPermissionsAction.value = AsyncAction.Uninitialized
                }
                is RolesAndPermissionsEvents.DemoteSelfTo -> coroutineScope.demoteSelfTo(
                    role = event.role,
                    changeOwnRoleAction = changeOwnRoleAction,
                )
                is RolesAndPermissionsEvents.ResetPermissions -> if (resetPermissionsAction.value.isConfirming()) {
                    coroutineScope.resetPermissions(resetPermissionsAction)
                } else {
                    resetPermissionsAction.value = AsyncAction.ConfirmingNoParams
                }
            }
        }

        return RolesAndPermissionsState(
            roomSupportsOwnerRole = roomInfo.privilegedCreatorRole,
            adminCount = adminCount,
            moderatorCount = moderatorCount,
            canDemoteSelf = canDemoteSelf.value,
            changeOwnRoleAction = changeOwnRoleAction.value,
            resetPermissionsAction = resetPermissionsAction.value,
            eventSink = { handleEvent(it) },
        )
    }

    private fun CoroutineScope.demoteSelfTo(
        role: RoomMember.Role,
        changeOwnRoleAction: MutableState<AsyncAction<Unit>>,
    ) = launch(dispatchers.io) {
        runUpdatingState(changeOwnRoleAction) {
            room.updateUsersRoles(listOf(UserRoleChange(room.sessionId, role)))
        }
    }

    private fun CoroutineScope.resetPermissions(
        resetPermissionsAction: MutableState<AsyncAction<Unit>>,
    ) = launch(dispatchers.io) {
        runUpdatingState(resetPermissionsAction) {
            analyticsService.capture(RoomModeration(RoomModeration.Action.ResetPermissions))
            room.resetPowerLevels()
        }
    }

    private fun RoomInfo.userCountWithRole(userIds: List<UserId>, role: RoomMember.Role): Int {
        return usersWithRole(role).filter { it in userIds }.size
    }
}
