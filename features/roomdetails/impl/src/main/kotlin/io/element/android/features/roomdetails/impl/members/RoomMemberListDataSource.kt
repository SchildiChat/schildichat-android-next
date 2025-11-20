/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.members

import dev.zacsweers.metro.Inject
import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.room.BaseRoom
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomMembershipState
import io.element.android.libraries.matrix.api.room.roomMembers
import kotlinx.coroutines.withContext
import kotlin.collections.filter

@Inject
class RoomMemberListDataSource(
    private val room: BaseRoom,
    private val coroutineDispatchers: CoroutineDispatchers,
) {
    suspend fun search(query: String, selectedSection: SelectedSection): List<RoomMember> = withContext(coroutineDispatchers.io) {
        val roomMembersState = room.membersStateFlow.value
        val displayableMembers = roomMembersState.roomMembers()
            .orEmpty()
            .filter {
                when(selectedSection){
                    SelectedSection.MEMBERS -> it.membership.isActive()
                    SelectedSection.BANNED -> it.membership == RoomMembershipState.BAN
                }
            }

        val filteredMembers = if (query.isBlank()) {
            displayableMembers
        } else {
            displayableMembers.filter { member ->
                member.userId.value.contains(query, ignoreCase = true) ||
                    member.displayName?.contains(query, ignoreCase = true).orFalse()
            }
        }
        filteredMembers
    }
}
