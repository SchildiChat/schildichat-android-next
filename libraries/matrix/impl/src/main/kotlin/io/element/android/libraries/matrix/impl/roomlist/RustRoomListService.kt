/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.roomlist

import chat.schildi.lib.preferences.ScPreferencesStore
import chat.schildi.lib.preferences.ScPrefs
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.roomlist.DynamicRoomList
import io.element.android.libraries.matrix.api.roomlist.RoomList
import io.element.android.libraries.matrix.api.roomlist.RoomListFilter
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.api.roomlist.ScSdkInboxSettings
import io.element.android.libraries.matrix.api.roomlist.ScSdkRoomSortOrder
import io.element.android.libraries.matrix.api.roomlist.loadAllIncrementally
import io.element.android.libraries.matrix.impl.room.RoomSyncSubscriber
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import org.matrix.rustcomponents.sdk.RoomListServiceState
import org.matrix.rustcomponents.sdk.RoomListServiceSyncIndicator
import timber.log.Timber
import org.matrix.rustcomponents.sdk.RoomListService as InnerRustRoomListService

private const val DEFAULT_PAGE_SIZE = 20

internal class RustRoomListService(
    private val innerRoomListService: InnerRustRoomListService,
    private val sessionDispatcher: CoroutineDispatcher,
    private val roomListFactory: RoomListFactory,
    private val roomSyncSubscriber: RoomSyncSubscriber,
    private val scPreferencesStore: ScPreferencesStore,
    sessionCoroutineScope: CoroutineScope,
) : RoomListService {

    override fun createRoomList(
        pageSize: Int,
        initialFilter: RoomListFilter,
        source: RoomList.Source
    ): DynamicRoomList {
        return roomListFactory.createRoomList(
            pageSize = pageSize,
            initialFilter = initialFilter,
            coroutineContext = sessionDispatcher,
        ) {
            when (source) {
                RoomList.Source.All -> innerRoomListService.allRooms()
                RoomList.Source.SPACES -> innerRoomListService.allSpaces()
            }
        }
    }

    override suspend fun subscribeToVisibleRooms(roomIds: List<RoomId>) {
        roomSyncSubscriber.batchSubscribe(roomIds)
    }

    override val allRooms: DynamicRoomList = roomListFactory.createRoomList(
        pageSize = DEFAULT_PAGE_SIZE,
        coroutineContext = sessionDispatcher,
        initialInboxSettings = ScSdkInboxSettings(
            sortOrder = ScSdkRoomSortOrder(
                byUnread = scPreferencesStore.getCachedOrDefaultValue(ScPrefs.SORT_BY_UNREAD),
                pinFavourites = scPreferencesStore.getCachedOrDefaultValue(ScPrefs.PIN_FAVORITES),
                buryLowPriority = scPreferencesStore.getCachedOrDefaultValue(ScPrefs.BURY_LOW_PRIORITY),
                clientSideUnreadCounts = scPreferencesStore.getCachedOrDefaultValue(ScPrefs.CLIENT_GENERATED_UNREAD_COUNTS),
                withSilentUnread = scPreferencesStore.getCachedOrDefaultValue(ScPrefs.SORT_WITH_SILENT_UNREAD),
            )
        ),
    ) {
        innerRoomListService.allRooms()
    }

    override val allSpaces: DynamicRoomList = roomListFactory.createRoomList(
        pageSize = DEFAULT_PAGE_SIZE,
        coroutineContext = sessionDispatcher,
    ) {
        innerRoomListService.allSpaces()
    }

    init {
        allRooms.loadAllIncrementally(sessionCoroutineScope)
        allSpaces.loadAllIncrementally(sessionCoroutineScope)
    }

    override val syncIndicator: StateFlow<RoomListService.SyncIndicator> =
        innerRoomListService.syncIndicator()
            .map { it.toSyncIndicator() }
            .onEach { syncIndicator ->
                Timber.d("SyncIndicator = $syncIndicator")
            }
            .distinctUntilChanged()
            .stateIn(sessionCoroutineScope, SharingStarted.Eagerly, RoomListService.SyncIndicator.Hide)

    override val state: StateFlow<RoomListService.State> =
        innerRoomListService.stateFlow()
            .map { it.toRoomListState() }
            .onEach { state ->
                Timber.d("RoomList state=$state")
            }
            .distinctUntilChanged()
            .stateIn(sessionCoroutineScope, SharingStarted.Eagerly, RoomListService.State.Idle)
}

private fun RoomListServiceState.toRoomListState(): RoomListService.State {
    return when (this) {
        RoomListServiceState.INITIAL,
        RoomListServiceState.RECOVERING,
        RoomListServiceState.SETTING_UP -> RoomListService.State.Idle
        RoomListServiceState.RUNNING -> RoomListService.State.Running
        RoomListServiceState.ERROR -> RoomListService.State.Error
        RoomListServiceState.TERMINATED -> RoomListService.State.Terminated
    }
}

private fun RoomListServiceSyncIndicator.toSyncIndicator(): RoomListService.SyncIndicator {
    return when (this) {
        RoomListServiceSyncIndicator.SHOW -> RoomListService.SyncIndicator.Show
        RoomListServiceSyncIndicator.HIDE -> RoomListService.SyncIndicator.Hide
    }
}
