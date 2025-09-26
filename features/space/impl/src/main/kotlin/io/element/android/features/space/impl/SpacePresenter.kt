/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.Inject
import im.vector.app.features.analytics.plan.JoinedRoom
import io.element.android.features.invite.api.SeenInvitesStore
import io.element.android.features.space.api.SpaceEntryPoint
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.coroutine.mapState
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.join.JoinRoom
import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import io.element.android.libraries.matrix.api.spaces.SpaceRoomList
import io.element.android.libraries.matrix.ui.safety.rememberHideInvitesAvatar
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.jvm.optionals.getOrNull

@Inject class SpacePresenter(
    @Assisted private val inputs: SpaceEntryPoint.Inputs,
    private val client: MatrixClient,
    private val seenInvitesStore: SeenInvitesStore,
    private val joinRoom: JoinRoom,
    @SessionCoroutineScope private val sessionCoroutineScope: CoroutineScope,
) : Presenter<SpaceState> {
    @AssistedFactory fun interface Factory {
        fun create(inputs: SpaceEntryPoint.Inputs): SpacePresenter
    }

    private val spaceRoomList = client.spaceService.spaceRoomList(inputs.roomId)

    @Composable
    override fun present(): SpaceState {
        LaunchedEffect(Unit) {
            paginate()
        }
        val hideInvitesAvatar by client.rememberHideInvitesAvatar()
        val seenSpaceInvites by remember {
            seenInvitesStore.seenRoomIds().map { it.toPersistentSet() }
        }.collectAsState(persistentSetOf())

        val localCoroutineScope = rememberCoroutineScope()
        val children by spaceRoomList.spaceRoomsFlow.collectAsState(emptyList())
        val hasMoreToLoad by remember {
            spaceRoomList.paginationStatusFlow.mapState { status ->
                when (status) {
                    is SpaceRoomList.PaginationStatus.Idle -> status.hasMoreToLoad
                    SpaceRoomList.PaginationStatus.Loading -> true
                }
            }
        }.collectAsState()

        val currentSpace by spaceRoomList.currentSpaceFlow.collectAsState()
        val joiningRooms = remember { mutableStateOf(emptySet<RoomId>()) }

        LaunchedEffect(children, joiningRooms.value) {
            val joinedChildren = children
                .filter { it.state == CurrentUserMembership.JOINED }
                .map { it.roomId }
                .toSet()
            joiningRooms.value.let { currentlyJoining ->
                joiningRooms.value = currentlyJoining - joinedChildren
            }
        }

        fun handleEvents(event: SpaceEvents) {
            when (event) {
                SpaceEvents.LoadMore -> localCoroutineScope.paginate()
                is SpaceEvents.Join -> {
                    sessionCoroutineScope.joinRoom(event.spaceRoom, joiningRooms)
                }
            }
        }
        return SpaceState(
            currentSpace = currentSpace.getOrNull(),
            children = children.toPersistentList(),
            seenSpaceInvites = seenSpaceInvites,
            hideInvitesAvatar = hideInvitesAvatar,
            hasMoreToLoad = hasMoreToLoad,
            joiningRooms = joiningRooms.value.toPersistentSet(),
            eventSink = ::handleEvents,
        )
    }

    private fun CoroutineScope.joinRoom(
        spaceRoom: SpaceRoom, joiningRooms: MutableState<Set<RoomId>>
    ) = launch {
        joiningRooms.value = joiningRooms.value + spaceRoom.roomId
        joinRoom.invoke(
            roomIdOrAlias = spaceRoom.roomId.toRoomIdOrAlias(),
            serverNames = spaceRoom.via,
            trigger = JoinedRoom.Trigger.SpaceHierarchy,
        ).onFailure {
            joiningRooms.value = joiningRooms.value - spaceRoom.roomId
        }
    }

    private fun CoroutineScope.paginate() = launch {
        spaceRoomList.paginate()
    }
}
