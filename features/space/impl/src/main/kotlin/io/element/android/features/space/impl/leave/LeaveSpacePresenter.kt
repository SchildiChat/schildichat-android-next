/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl.leave

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.map
import io.element.android.libraries.architecture.runUpdatingState
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.spaces.LeaveSpaceHandle
import io.element.android.libraries.matrix.api.spaces.LeaveSpaceRoom
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@AssistedInject
class LeaveSpacePresenter(
    @Assisted private val leaveSpaceHandle: LeaveSpaceHandle,
) : Presenter<LeaveSpaceState> {
    @AssistedFactory
    fun interface Factory {
        fun create(leaveSpaceHandle: LeaveSpaceHandle): LeaveSpacePresenter
    }

    @Composable
    override fun present(): LeaveSpaceState {
        val coroutineScope = rememberCoroutineScope()
        var currentSpace: LeaveSpaceRoom? by remember { mutableStateOf(null) }
        val leaveSpaceAction = remember {
            mutableStateOf<AsyncAction<Unit>>(AsyncAction.Uninitialized)
        }
        val selectedRoomIds = remember {
            mutableStateOf<ImmutableSet<RoomId>>(persistentSetOf())
        }
        val leaveSpaceRooms by produceState(AsyncData.Loading()) {
            val rooms = leaveSpaceHandle.rooms()
            val (currentRoom, otherRooms) = rooms.getOrNull()
                .orEmpty()
                .partition { it.spaceRoom.roomId == leaveSpaceHandle.id }
            currentSpace = currentRoom.firstOrNull()
            // By default select all rooms that can be left
            selectedRoomIds.value = otherRooms
                .filter { it.isLastAdmin.not() }
                .map { it.spaceRoom.roomId }
                .toPersistentSet()
            value = rooms.fold(
                onSuccess = { AsyncData.Success(otherRooms) },
                onFailure = { AsyncData.Failure(it) }
            )
        }
        val selectableSpaceRooms by produceState(
            initialValue = AsyncData.Loading(),
            key1 = leaveSpaceRooms,
            key2 = selectedRoomIds.value,
        ) {
            value = leaveSpaceRooms.map { list ->
                list.orEmpty().map { room ->
                    SelectableSpaceRoom(
                        spaceRoom = room.spaceRoom,
                        isLastAdmin = room.isLastAdmin,
                        isSelected = selectedRoomIds.value.contains(room.spaceRoom.roomId),
                    )
                }.toImmutableList()
            }
        }

        fun handleEvents(event: LeaveSpaceEvents) {
            when (event) {
                LeaveSpaceEvents.DeselectAllRooms -> {
                    selectedRoomIds.value = persistentSetOf()
                }
                LeaveSpaceEvents.SelectAllRooms -> {
                    selectedRoomIds.value = selectableSpaceRooms.dataOrNull()
                        .orEmpty()
                        .filter { it.isLastAdmin.not() }
                        .map { it.spaceRoom.roomId }
                        .toPersistentSet()
                }
                is LeaveSpaceEvents.ToggleRoomSelection -> {
                    val currentSet = selectedRoomIds.value
                    selectedRoomIds.value = if (currentSet.contains(event.roomId)) {
                        currentSet - event.roomId
                    } else {
                        currentSet + event.roomId
                    }
                        .toPersistentSet()
                }
                LeaveSpaceEvents.LeaveSpace -> coroutineScope.leaveSpace(
                    leaveSpaceAction = leaveSpaceAction,
                    selectedRoomIds = selectedRoomIds.value,
                )
                LeaveSpaceEvents.CloseError -> {
                    leaveSpaceAction.value = AsyncAction.Uninitialized
                }
            }
        }

        return LeaveSpaceState(
            spaceName = currentSpace?.spaceRoom?.name,
            isLastAdmin = currentSpace?.isLastAdmin == true,
            selectableSpaceRooms = selectableSpaceRooms,
            leaveSpaceAction = leaveSpaceAction.value,
            eventSink = ::handleEvents,
        )
    }

    private fun CoroutineScope.leaveSpace(
        leaveSpaceAction: MutableState<AsyncAction<Unit>>,
        selectedRoomIds: Set<RoomId>,
    ) = launch {
        runUpdatingState(leaveSpaceAction) {
            leaveSpaceHandle.leave(selectedRoomIds.toList())
        }
    }
}
