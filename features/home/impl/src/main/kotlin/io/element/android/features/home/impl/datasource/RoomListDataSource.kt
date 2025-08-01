/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.datasource

import chat.schildi.features.home.ScInboxSettingsSource
import chat.schildi.lib.preferences.ScPreferencesStore
import io.element.android.features.home.impl.model.RoomListRoomSummary
import io.element.android.libraries.androidutils.diff.DiffCacheUpdater
import io.element.android.libraries.androidutils.diff.MutableListDiffCache
import io.element.android.libraries.androidutils.system.DateTimeObserver
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.notificationsettings.NotificationSettingsService
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class RoomListDataSource @Inject constructor(
    private val roomListService: RoomListService,
    private val roomListRoomSummaryFactory: RoomListRoomSummaryFactory,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val notificationSettingsService: NotificationSettingsService,
    private val scInboxSettingsSource: ScInboxSettingsSource,
    scPreferencesStore: ScPreferencesStore,
    @SessionCoroutineScope
    private val sessionCoroutineScope: CoroutineScope,
    private val dateTimeObserver: DateTimeObserver,
) {
    init {
        observeNotificationSettings()
        observeDateTimeChanges()
    }

    private val _allRooms = MutableSharedFlow<ImmutableList<RoomListRoomSummary>>(replay = 1)

    private val lock = Mutex()
    private val diffCache = MutableListDiffCache<RoomListRoomSummary>()
    private val diffCacheUpdater = DiffCacheUpdater<RoomSummary, RoomListRoomSummary>(diffCache = diffCache, detectMoves = true) { old, new ->
        old?.roomId == new?.roomId
    }

    val allRooms: Flow<ImmutableList<RoomListRoomSummary>> = _allRooms.applyInviteFilterSetting(scPreferencesStore)

    val loadingState = roomListService.allRooms.loadingState

    fun launchIn(coroutineScope: CoroutineScope) {
        scInboxSettingsSource.launchIn(coroutineScope)
        roomListService
            .allRooms
            .filteredSummaries
            .onEach { roomSummaries ->
                replaceWith(roomSummaries)
            }
            .launchIn(coroutineScope)
    }

    suspend fun subscribeToVisibleRooms(roomIds: List<RoomId>) {
        roomListService.subscribeToVisibleRooms(roomIds)
    }

    @OptIn(FlowPreview::class)
    private fun observeNotificationSettings() {
        notificationSettingsService.notificationSettingsChangeFlow
            .debounce(0.5.seconds)
            .onEach {
                roomListService.allRooms.rebuildSummaries()
            }
            .launchIn(sessionCoroutineScope)
    }

    private fun observeDateTimeChanges() {
        dateTimeObserver.changes
            .onEach { event ->
                when (event) {
                    is DateTimeObserver.Event.TimeZoneChanged -> rebuildAllRoomSummaries()
                    is DateTimeObserver.Event.DateChanged -> rebuildAllRoomSummaries()
                }
            }
            .launchIn(sessionCoroutineScope)
    }

    private suspend fun replaceWith(roomSummaries: List<RoomSummary>) = withContext(coroutineDispatchers.computation) {
        lock.withLock {
            diffCacheUpdater.updateWith(roomSummaries)
            buildAndEmitAllRooms(roomSummaries)
        }
    }

    private suspend fun buildAndEmitAllRooms(roomSummaries: List<RoomSummary>, useCache: Boolean = true) {
        val roomListRoomSummaries = diffCache.indices().mapNotNull { index ->
            if (useCache) {
                diffCache.get(index) ?: buildAndCacheItem(roomSummaries, index)
            } else {
                buildAndCacheItem(roomSummaries, index)
            }
        }
        _allRooms.emit(roomListRoomSummaries.toImmutableList())
    }

    private fun buildAndCacheItem(roomSummaries: List<RoomSummary>, index: Int): RoomListRoomSummary? {
        val roomListSummary = roomSummaries.getOrNull(index)?.let { roomListRoomSummaryFactory.create(it) }
        diffCache[index] = roomListSummary
        return roomListSummary
    }

    private suspend fun rebuildAllRoomSummaries() {
        lock.withLock {
            roomListService.allRooms.filteredSummaries.replayCache.firstOrNull()?.let { roomSummaries ->
                buildAndEmitAllRooms(roomSummaries, useCache = false)
            }
        }
    }
}
