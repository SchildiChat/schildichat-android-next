/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl.root

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.invite.api.acceptdecline.AcceptDeclineInviteState
import io.element.android.features.invite.api.acceptdecline.anAcceptDeclineInviteState
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import io.element.android.libraries.previewutils.room.aSpaceRoom
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toImmutableSet

open class SpaceStateProvider : PreviewParameterProvider<SpaceState> {
    override val values: Sequence<SpaceState>
        get() = sequenceOf(
            aSpaceState(),
            aSpaceState(
                parentSpace = aSpaceRoom(
                    name = null,
                    numJoinedMembers = 5,
                    childrenCount = 10,
                    worldReadable = true,
                ),
                hasMoreToLoad = true,
            ),
            aSpaceState(
                hasMoreToLoad = true,
                children = aListOfSpaceRooms(),
            ),
            aSpaceState(
                hasMoreToLoad = false,
                children = aListOfSpaceRooms(),
                joiningRooms = setOf(RoomId("!spaceId0:example.com")),
            )
            // Add other states here
        )
}

fun aSpaceState(
    parentSpace: SpaceRoom? = aSpaceRoom(
        numJoinedMembers = 5,
        childrenCount = 10,
        worldReadable = true,
        roomId = RoomId("!spaceId0:example.com"),
    ),
    children: List<SpaceRoom> = emptyList(),
    seenSpaceInvites: Set<RoomId> = emptySet(),
    joiningRooms: Set<RoomId> = emptySet(),
    joinActions: Map<RoomId, AsyncAction<Unit>> = joiningRooms.associateWith { AsyncAction.Loading },
    hideInvitesAvatar: Boolean = false,
    hasMoreToLoad: Boolean = false,
    acceptDeclineInviteState: AcceptDeclineInviteState = anAcceptDeclineInviteState(),
) = SpaceState(
    currentSpace = parentSpace,
    children = children.toImmutableList(),
    seenSpaceInvites = seenSpaceInvites.toImmutableSet(),
    hideInvitesAvatar = hideInvitesAvatar,
    hasMoreToLoad = hasMoreToLoad,
    joinActions = joinActions.toImmutableMap(),
    acceptDeclineInviteState = acceptDeclineInviteState,
    eventSink = {}
)

private fun aListOfSpaceRooms(): List<SpaceRoom> {
    return listOf(
        aSpaceRoom(
            roomId = RoomId("!spaceId0:example.com"),
            state = null,
        ),
        aSpaceRoom(
            roomId = RoomId("!spaceId1:example.com"),
            state = CurrentUserMembership.JOINED,
        ),
        aSpaceRoom(
            roomId = RoomId("!spaceId2:example.com"),
            state = CurrentUserMembership.INVITED,
        ),
    )
}
