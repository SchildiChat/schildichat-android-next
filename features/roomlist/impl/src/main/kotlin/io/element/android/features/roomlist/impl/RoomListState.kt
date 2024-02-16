/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.roomlist.impl

import androidx.compose.runtime.Immutable
import io.element.android.features.leaveroom.api.LeaveRoomState
import chat.schildi.features.roomlist.spaces.SpaceListDataSource
import chat.schildi.features.roomlist.spaces.SpaceUnreadCountsDataSource
import io.element.android.features.roomlist.impl.model.RoomListRoomSummary
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarMessage
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.user.MatrixUser
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf

@Immutable
data class RoomListState(
    val matrixUser: MatrixUser?,
    val showAvatarIndicator: Boolean,
    val roomList: AsyncData<ImmutableList<RoomListRoomSummary>>,
    val spacesList: ImmutableList<SpaceListDataSource.SpaceHierarchyItem> = persistentListOf(),
    val spaceSelectionHierarchy: ImmutableList<String> = persistentListOf(),
    val spaceUnreadCounts: ImmutableMap<String?, SpaceUnreadCountsDataSource.SpaceUnreadCounts> = persistentMapOf(),
    val filter: String?,
    val filteredRoomList: ImmutableList<RoomListRoomSummary>,
    val displayVerificationPrompt: Boolean,
    val displayRecoveryKeyPrompt: Boolean,
    val hasNetworkConnection: Boolean,
    val snackbarMessage: SnackbarMessage?,
    val invitesState: InvitesState,
    val displaySearchResults: Boolean,
    val contextMenu: ContextMenu,
    val leaveRoomState: LeaveRoomState,
    val displayMigrationStatus: Boolean,
    val eventSink: (RoomListEvents) -> Unit,
) {
    sealed interface ContextMenu {
        data object Hidden : ContextMenu
        data class Shown(
            val roomId: RoomId,
            val roomName: String,
            val isUnread: Boolean = false,
            val isDm: Boolean,
            val markAsUnreadFeatureFlagEnabled: Boolean,
            val hasNewContent: Boolean,
        ) : ContextMenu
    }
}

enum class InvitesState {
    NoInvites,
    SeenInvites,
    NewInvites,
}
