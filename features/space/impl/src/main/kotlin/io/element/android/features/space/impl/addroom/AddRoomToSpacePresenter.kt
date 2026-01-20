/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl.addroom

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Inject
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runUpdatingState
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.isDm
import io.element.android.libraries.matrix.api.room.recent.getRecentRooms
import io.element.android.libraries.matrix.api.spaces.SpaceRoomList
import io.element.android.libraries.matrix.api.spaces.SpaceService
import io.element.android.libraries.matrix.ui.model.SelectRoomInfo
import io.element.android.libraries.matrix.ui.model.toSelectRoomInfo
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch

private const val MAX_SUGGESTIONS_COUNT = 5

@Inject
class AddRoomToSpacePresenter(
    private val spaceRoomList: SpaceRoomList,
    private val dataSource: AddRoomToSpaceSearchDataSource,
    private val spaceService: SpaceService,
    private val matrixClient: MatrixClient,
    @SessionCoroutineScope private val sessionCoroutineScope: CoroutineScope,
) : Presenter<AddRoomToSpaceState> {

    @Composable
    override fun present(): AddRoomToSpaceState {
        var selectedRooms by remember { mutableStateOf(persistentListOf<SelectRoomInfo>()) }
        var searchQuery by remember { mutableStateOf("") }
        var isSearchActive by remember { mutableStateOf(false) }
        val saveAction = remember { mutableStateOf<AsyncAction<Unit>>(AsyncAction.Uninitialized) }

        // Load data source
        LaunchedEffect(Unit) { dataSource.load() }

        // Update search query in data source
        LaunchedEffect(searchQuery) {
            dataSource.setSearchQuery(searchQuery)
        }

        // Get rooms already in space
        val spaceChildrenIds by remember {
            spaceRoomList.spaceRoomsFlow.map { spaceChildren ->
                spaceChildren.map { it.roomId }
            }
        }.collectAsState(initial = emptyList())

        // Suggestions from recently visited rooms (excluding DMs, spaces, and rooms already in space)
        val suggestions by produceState(persistentListOf(), spaceChildrenIds) {
            value = matrixClient
                .getRecentRooms { info ->
                    !info.isSpace && !info.isDm && info.currentUserMembership == CurrentUserMembership.JOINED
                }
                .take(MAX_SUGGESTIONS_COUNT)
                .map { info -> info.toSelectRoomInfo() }
                .toList()
                .toImmutableList()
        }

        val allRooms by dataSource.roomInfoList.collectAsState(initial = persistentListOf())
        val searchResults by remember<State<SearchBarResultState<ImmutableList<SelectRoomInfo>>>> {
            derivedStateOf {
                val filtered = allRooms.filterNot { it.roomId in spaceChildrenIds }
                when {
                    filtered.isNotEmpty() -> SearchBarResultState.Results(filtered.toImmutableList())
                    isSearchActive && searchQuery.isNotEmpty() -> SearchBarResultState.NoResultsFound()
                    else -> SearchBarResultState.Initial()
                }
            }
        }

        fun handleEvent(event: AddRoomToSpaceEvents) {
            when (event) {
                is AddRoomToSpaceEvents.ToggleRoom -> {
                    selectedRooms = if (selectedRooms.any { it.roomId == event.room.roomId }) {
                        selectedRooms.filterNot { it.roomId == event.room.roomId }.toPersistentList()
                    } else {
                        (selectedRooms + event.room).toPersistentList()
                    }
                }
                is AddRoomToSpaceEvents.UpdateSearchQuery -> {
                    searchQuery = event.query
                }
                is AddRoomToSpaceEvents.OnSearchActiveChanged -> {
                    isSearchActive = event.active
                    if (!event.active) {
                        searchQuery = ""
                    }
                }
                AddRoomToSpaceEvents.CloseSearch -> {
                    isSearchActive = false
                    searchQuery = ""
                }
                AddRoomToSpaceEvents.Save -> {
                    sessionCoroutineScope.addRoomsToSpace(
                        selectedRooms = selectedRooms,
                        addAction = saveAction,
                    )
                }
                AddRoomToSpaceEvents.ClearError -> {
                    saveAction.value = AsyncAction.Uninitialized
                }
            }
        }

        return AddRoomToSpaceState(
            searchQuery = searchQuery,
            isSearchActive = isSearchActive,
            searchResults = searchResults,
            selectedRooms = selectedRooms,
            suggestions = suggestions,
            saveAction = saveAction.value,
            eventSink = ::handleEvent,
        )
    }

    private fun CoroutineScope.addRoomsToSpace(
        selectedRooms: ImmutableList<SelectRoomInfo>,
        addAction: MutableState<AsyncAction<Unit>>,
    ) = launch {
        addAction.runUpdatingState {
            val results = selectedRooms.map { selectedRoom ->
                async {
                    spaceService.addChildToSpace(
                        spaceId = spaceRoomList.roomId,
                        childId = selectedRoom.roomId,
                    )
                }
            }.awaitAll()
            val anyFailure = results.any { it.isFailure }
            if (anyFailure) {
                Result.failure(Exception("Failed to add some rooms"))
            } else {
                Result.success(Unit)
            }
        }
    }
}
