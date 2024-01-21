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

package io.element.android.features.roomlist.impl.datasource

import chat.schildi.lib.preferences.ScAppStateStore
import io.element.android.features.roomlist.impl.model.RoomListRoomSummary
import io.element.android.features.roomlist.impl.model.RoomListRoomSummaryPlaceholders
import io.element.android.libraries.androidutils.diff.DiffCacheUpdater
import io.element.android.libraries.androidutils.diff.MutableListDiffCache
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.extensions.orEmpty
import io.element.android.libraries.dateformatter.api.LastMessageTimestampFormatter
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.eventformatter.api.RoomLastMessageFormatter
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.notificationsettings.NotificationSettingsService
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class RoomListDataSource @Inject constructor(
    private val client: MatrixClient,
    private val roomListService: RoomListService,
    private val lastMessageTimestampFormatter: LastMessageTimestampFormatter,
    private val roomLastMessageFormatter: RoomLastMessageFormatter,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val notificationSettingsService: NotificationSettingsService,
    private val appScope: CoroutineScope,
) {
    init {
        observeNotificationSettings()
    }

    private val _filter = MutableStateFlow("")
    private val _spaceSelectionHierarchy = MutableStateFlow<ImmutableList<String>?>(null)
    private val _spaceChildFilter = MutableStateFlow<ImmutableList<String>?>(null)
    private val _allRooms = MutableStateFlow<ImmutableList<RoomListRoomSummary>>(persistentListOf())
    private val _filteredRooms = MutableStateFlow<ImmutableList<RoomListRoomSummary>>(persistentListOf())
    private val _spaceRooms = MutableStateFlow<ImmutableList<RoomListRoomSummary>>(persistentListOf())

    private val lock = Mutex()
    private val diffCache = MutableListDiffCache<RoomListRoomSummary>()
    private val diffCacheUpdater = DiffCacheUpdater<RoomSummary, RoomListRoomSummary>(diffCache = diffCache, detectMoves = true) { old, new ->
        old?.identifier() == new?.identifier()
    }

    @OptIn(FlowPreview::class)
    fun launchIn(coroutineScope: CoroutineScope, spaceListDataSource: SpaceListDataSource, scAppStateStore: ScAppStateStore) {
        roomListService
            .allRooms
            .summaries
            .onEach { roomSummaries ->
                replaceWith(roomSummaries)
            }
            .launchIn(coroutineScope)

        combine(
            _filter,
            _allRooms
        ) { filterValue, allRoomsValue ->
            when {
                filterValue.isEmpty() -> emptyList()
                else -> allRoomsValue.filter { it.name.contains(filterValue, ignoreCase = true) }
            }.toImmutableList()
        }
            .onEach {
                _filteredRooms.value = it
            }
            .launchIn(coroutineScope)

        // Restore previous space selection
        coroutineScope.launch {
            _spaceSelectionHierarchy.value = scAppStateStore.loadInitialSpaceSelection().toImmutableList()
        }
        // Persist space selection to restore on next app launch
        _spaceSelectionHierarchy.debounce(2000).onEach { selection ->
            if (selection == null) {
                // Not initialized yet
                return@onEach
            }
            scAppStateStore.persistSpaceSelection(selection)
        }.launchIn(coroutineScope)

        // From life space list and current space selection, build the RoomId filter
        combine(
            _spaceSelectionHierarchy,
            spaceListDataSource.allSpaces,
        ) { spaceSelectionValue, allSpacesValue ->
            // No space selected or not initialized yet -> show all
            if (spaceSelectionValue.isNullOrEmpty()) {
                return@combine Pair(null, true)
            }
            // Show all rooms while space list is not loaded yet, without clearing the space hierarchy
            var spaceList = allSpacesValue?.takeIf { it.isNotEmpty() } ?: return@combine Pair(null, true)
            // Resolve actual space from space hierarchy
            var space: SpaceListDataSource.SpaceHierarchyItem? = null
            spaceSelectionValue.forEach { spaceId ->
                space = spaceList.find { it.info.roomId.value == spaceId }
                if (space == null) {
                    return@combine Pair(null, false)
                }
                spaceList = space!!.spaces
            }
            // Tell SDK we filter the sliding sync window by spaces
            space?.let {
                client.roomListService.updateVisibleSpaces(it.flattenedSpaces)
            }
            return@combine Pair(space?.flattenedRooms, true)
        }
            .onEach { (roomIds, spaceFound) ->
                _spaceChildFilter.value = roomIds
                if (!spaceFound) {
                    Timber.i("Selected space not found, clearing selection")
                    updateSpaceSelection(persistentListOf())
                }
            }
            .launchIn(coroutineScope)
        // Workaround to refresh m.space.child relations without listening to every room's state
        _spaceSelectionHierarchy.filter { !it.isNullOrEmpty() }.drop(1).debounce(500).onEach {
            spaceListDataSource.forceRebuildSpaceFilter()
        }.launchIn(coroutineScope)

        // Filter by space with the room id list built in the previous flow
        combine(
            _spaceChildFilter,
            _allRooms
        ) { filterValue, allRoomsValue ->
            // Do the actual filtering
            when (filterValue) {
                null -> allRoomsValue
                else -> allRoomsValue.filter { filterValue.contains(it.roomId.value) }
            }.toImmutableList()
        }
            .onEach {
                _spaceRooms.value = it
            }
            .launchIn(coroutineScope)
    }

    fun updateFilter(filterValue: String) {
        _filter.value = filterValue
    }

    fun updateSpaceSelection(spaceSelectionHierarchy: ImmutableList<String>) {
        _spaceSelectionHierarchy.value = spaceSelectionHierarchy
    }

    val filter: StateFlow<String> = _filter
    val allRooms: StateFlow<ImmutableList<RoomListRoomSummary>> = _allRooms
    val filteredRooms: StateFlow<ImmutableList<RoomListRoomSummary>> = _filteredRooms
    val spaceRooms: StateFlow<ImmutableList<RoomListRoomSummary>> = _spaceRooms
    val spaceSelectionHierarchy: StateFlow<ImmutableList<String>?> = _spaceSelectionHierarchy

    @OptIn(FlowPreview::class)
    private fun observeNotificationSettings() {
        notificationSettingsService.notificationSettingsChangeFlow
            .debounce(0.5.seconds)
            .onEach {
                roomListService.allRooms.rebuildSummaries()
            }
            .launchIn(appScope)
    }

    private suspend fun replaceWith(roomSummaries: List<RoomSummary>) = withContext(coroutineDispatchers.computation) {
        lock.withLock {
            diffCacheUpdater.updateWith(roomSummaries)
            buildAndEmitAllRooms(roomSummaries)
        }
    }

    private suspend fun buildAndEmitAllRooms(roomSummaries: List<RoomSummary>) {
        if (diffCache.isEmpty()) {
            _allRooms.emit(
                RoomListRoomSummaryPlaceholders.createFakeList(16).toImmutableList()
            )
        } else {
            val roomListRoomSummaries = ArrayList<RoomListRoomSummary>()
            for (index in diffCache.indices()) {
                val cacheItem = diffCache.get(index)
                if (cacheItem == null) {
                    buildAndCacheItem(roomSummaries, index)?.also { timelineItemState ->
                        roomListRoomSummaries.add(timelineItemState)
                    }
                } else {
                    roomListRoomSummaries.add(cacheItem)
                }
            }
            _allRooms.emit(roomListRoomSummaries.toImmutableList())
        }
    }

    private fun buildAndCacheItem(roomSummaries: List<RoomSummary>, index: Int): RoomListRoomSummary? {
        val roomListRoomSummary = when (val roomSummary = roomSummaries.getOrNull(index)) {
            is RoomSummary.Empty -> RoomListRoomSummaryPlaceholders.create(roomSummary.identifier)
            is RoomSummary.Filled -> {
                val avatarData = AvatarData(
                    id = roomSummary.identifier(),
                    name = roomSummary.details.name,
                    url = roomSummary.details.avatarURLString,
                    size = AvatarSize.RoomListItem,
                )
                val roomIdentifier = roomSummary.identifier()
                RoomListRoomSummary(
                    id = roomSummary.identifier(),
                    roomId = RoomId(roomIdentifier),
                    name = roomSummary.details.name,
                    hasUnread = roomSummary.details.unreadNotificationCount > 0,
                    notificationCount = roomSummary.details.unreadNotificationCount,
                    highlightCount = roomSummary.details.highlightCount,
                    unreadCount = roomSummary.details.unreadCount,
                    timestamp = lastMessageTimestampFormatter.format(roomSummary.details.lastMessageTimestamp),
                    lastMessage = roomSummary.details.lastMessage?.let { message ->
                        roomLastMessageFormatter.format(message.event, roomSummary.details.isDirect)
                    }.orEmpty(),
                    avatarData = avatarData,
                    notificationMode = roomSummary.details.notificationMode,
                    hasOngoingCall = roomSummary.details.hasOngoingCall,
                    isDm = roomSummary.details.isDm,
                )
            }
            null -> null
        }

        diffCache[index] = roomListRoomSummary
        return roomListRoomSummary
    }
}
