/*
 * Copyright (c) 2023 New Vector Ltd
 * Copyright (c) 2024 SchildiChat
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

import androidx.compose.runtime.Immutable
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
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

/**
 * More or less a copy of RoomListDataSource, but for spaces and without filter
 */
class SpaceListDataSource @Inject constructor(
    private val client: MatrixClient,
    private val roomListService: RoomListService,
    private val lastMessageTimestampFormatter: LastMessageTimestampFormatter,
    private val roomLastMessageFormatter: RoomLastMessageFormatter,
    private val coroutineDispatchers: CoroutineDispatchers,
) {
    private val _allSpaces = MutableStateFlow<ImmutableList<SpaceHierarchyItem>>(persistentListOf())

    private val lock = Mutex()
    private val diffCache = MutableListDiffCache<RoomListRoomSummary>()
    private val diffCacheUpdater = DiffCacheUpdater<RoomSummary, RoomListRoomSummary>(diffCache = diffCache, detectMoves = true) { old, new ->
        old?.identifier() == new?.identifier()
    }

    fun launchIn(coroutineScope: CoroutineScope) {
        roomListService
            .allSpaces
            .summaries
            .onEach { roomSummaries ->
                replaceWith(roomSummaries)
            }
            .launchIn(coroutineScope)
    }

    val allSpaces: StateFlow<ImmutableList<SpaceHierarchyItem>> = _allSpaces

    private suspend fun replaceWith(roomSummaries: List<RoomSummary>) = withContext(coroutineDispatchers.computation) {
        lock.withLock {
            diffCacheUpdater.updateWith(roomSummaries)
            buildAndEmitAllSpaces(roomSummaries)
        }
    }

    private suspend fun buildAndEmitAllSpaces(roomSummaries: List<RoomSummary>) {
        if (diffCache.isEmpty()) {
            _allSpaces.emit(persistentListOf())
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
            _allSpaces.emit(buildSpaceHierarchy(roomListRoomSummaries))
        }
    }

    /**
     * Build the space hierarchy and avoid loops
     */
    // TODO what can we cache something here?
    private suspend fun buildSpaceHierarchy(spaceSummaries: List<RoomListRoomSummary>): ImmutableList<SpaceHierarchyItem> {
        // Map spaceId -> list of child spaces
        val spaceHierarchyMap = HashMap<String, MutableList<RoomListRoomSummary>>()
        // Map spaceId -> list of regular child rooms
        val regularChildren = HashMap<String, MutableList<String>>()
        val rootSpaces = HashSet<RoomListRoomSummary>(spaceSummaries)
        spaceSummaries.forEach { parentSpace ->
            val spaceInfo = client.getRoom(parentSpace.roomId)
            val spaceChildren = spaceInfo?.spaceChildren
            spaceChildren?.forEach childLoop@{ childId ->
                val child = spaceSummaries.find { it.roomId.value == childId }
                if (child == null) {
                    // Treat as regular child, since it doesn't appear to be a space (at least none known to us at this point)
                    regularChildren[parentSpace.roomId.value] = regularChildren[parentSpace.roomId.value]?.apply { add(childId) } ?: mutableListOf(childId)
                    return@childLoop
                }
                rootSpaces.removeAll { it.roomId.value == childId }
                spaceHierarchyMap[parentSpace.roomId.value] = spaceHierarchyMap[parentSpace.roomId.value]?.apply { add(child) } ?: mutableListOf(child)
            }
        }

        // Build the actual immutable recursive data structures that replicate the hierarchy
        return rootSpaces.map { createSpaceHierarchyItem(it, spaceHierarchyMap, regularChildren) }.toImmutableList()
    }

    private fun createSpaceHierarchyItem(
        spaceSummary: RoomListRoomSummary,
        hierarchy: HashMap<String, MutableList<RoomListRoomSummary>>,
        regularChildren: HashMap<String, MutableList<String>>,
        forbiddenChildren: List<String> = emptyList(),
    ): SpaceHierarchyItem {
        val children = hierarchy[spaceSummary.id]?.mapNotNull {
            if (it.roomId.value in forbiddenChildren) {
                Timber.w("Detected space loop: ${spaceSummary.id} -> ${it.roomId.value}")
                null
            } else {
                createSpaceHierarchyItem(it, hierarchy, regularChildren, forbiddenChildren + listOf(spaceSummary.roomId.value))
            }
        }?.sortedBy{ it.info.name }?.toImmutableList() ?: persistentListOf()
        return SpaceHierarchyItem(
            info = spaceSummary,
            spaces = children,
            flattenedRooms = (
                // All direct space children
                regularChildren[spaceSummary.id].orEmpty()
                    // All direct space children spaces - actually not needed/wanted here
                    //+ hierarchy[spaceSummary.id].orEmpty().map {it.roomId.value} +
                    // All indirect space children
                    + children.flatMap { it.flattenedRooms }
            ).toImmutableList(),
        )
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
                )
            }
            null -> null
        }

        diffCache[index] = roomListRoomSummary
        return roomListRoomSummary
    }

    @Immutable
    data class SpaceHierarchyItem(
        val info: RoomListRoomSummary,
        val spaces: ImmutableList<SpaceHierarchyItem>,
        val flattenedRooms: ImmutableList<String>,
    )
}
