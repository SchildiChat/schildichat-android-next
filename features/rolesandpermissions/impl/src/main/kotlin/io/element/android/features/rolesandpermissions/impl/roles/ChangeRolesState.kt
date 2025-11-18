/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rolesandpermissions.impl.roles

import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.user.MatrixUser
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

data class ChangeRolesState(
    val role: RoomMember.Role,
    val query: String?,
    val isSearchActive: Boolean,
    val searchResults: SearchBarResultState<MembersByRole>,
    val selectedUsers: ImmutableList<MatrixUser>,
    val hasPendingChanges: Boolean,
    val savingState: AsyncAction<Boolean>,
    val canChangeMemberRole: (UserId) -> Boolean,
    val eventSink: (ChangeRolesEvent) -> Unit,
)

data class MembersByRole(
    val owners: ImmutableList<RoomMember>,
    val admins: ImmutableList<RoomMember>,
    val moderators: ImmutableList<RoomMember>,
    val members: ImmutableList<RoomMember>,
) {
    constructor() : this(
        owners = persistentListOf(),
        admins = persistentListOf(),
        moderators = persistentListOf(),
        members = persistentListOf(),
    )

    constructor(members: List<RoomMember>, comparator: Comparator<RoomMember>) : this(
        owners = members.filter { it.role is RoomMember.Role.Owner }.sorted(comparator),
        admins = members.filter { it.role == RoomMember.Role.Admin }.sorted(comparator),
        moderators = members.filter { it.role == RoomMember.Role.Moderator }.sorted(comparator),
        members = members.filter { it.role == RoomMember.Role.User }.sorted(comparator),
    )

    fun isEmpty() = owners.isEmpty() && admins.isEmpty() && moderators.isEmpty() && members.isEmpty()
}

private fun Iterable<RoomMember>.sorted(comparator: Comparator<RoomMember>): ImmutableList<RoomMember> {
    return sortedWith(comparator).toImmutableList()
}
