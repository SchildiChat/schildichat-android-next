/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.roomlist

import io.element.android.libraries.core.coroutine.childScope
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.roomlist.DynamicRoomList
import io.element.android.libraries.matrix.api.roomlist.RoomList
import io.element.android.libraries.matrix.api.roomlist.RoomListFilter
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.api.roomlist.ScRoomSortOrder
import io.element.android.libraries.matrix.api.roomlist.loadAllIncrementally
import io.element.android.libraries.matrix.impl.room.RoomSyncSubscriber
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import org.matrix.rustcomponents.sdk.RoomListServiceState
import org.matrix.rustcomponents.sdk.RoomListServiceSyncIndicator
import timber.log.Timber
import java.util.concurrent.atomic.AtomicReference
import org.matrix.rustcomponents.sdk.RoomListService as InnerRustRoomListService

private const val DEFAULT_PAGE_SIZE = 20

internal class RustRoomListService(
    private val innerRoomListService: InnerRustRoomListService,
    private val sessionCoroutineScope: CoroutineScope,
    private val sessionDispatcher: CoroutineDispatcher,
    private val roomListFactory: RoomListFactory,
    private val roomSyncSubscriber: RoomSyncSubscriber,
) : RoomListService {
    private val lastListWithSortOrder = AtomicReference<Triple<DynamicRoomList, CoroutineScope, ScRoomSortOrder>?>(null)

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

    override fun getOrReplaceRoomListWithSortOrder(
        pageSize: Int,
        initialFilter: RoomListFilter,
        sortOrder: ScRoomSortOrder
    ): DynamicRoomList {
        return lastListWithSortOrder.updateAndGet { previous ->
            if (previous?.third == sortOrder && previous.second.isActive) {
                previous
            } else {
                val scope = sessionCoroutineScope.childScope(sessionDispatcher, "sc-room-list")
                Triple(
                    roomListFactory.createRoomList(
                        pageSize = pageSize,
                        initialFilter = previous?.first?.currentFilter?.value ?: initialFilter,
                        coroutineScope = scope,
                        sortOrder = sortOrder,
                    ) {
                        innerRoomListService.allRooms()
                    }.also { roomList ->
                        previous?.second?.cancel("Sorted room list being replaced")
                        roomList.loadAllIncrementally(scope)
                    },
                    scope,
                    sortOrder
                )
            }
        }!!.first
    }

    override suspend fun subscribeToVisibleRooms(roomIds: List<RoomId>) {
        val toSubscribe = roomIds.filterNot { roomSyncSubscriber.isSubscribedTo(it) }
        if (toSubscribe.isNotEmpty()) {
            Timber.d("Subscribe to ${toSubscribe.size} rooms: $toSubscribe")
            roomSyncSubscriber.batchSubscribe(toSubscribe)
        }
    }

    override val allRooms: DynamicRoomList = roomListFactory.createRoomList(
        pageSize = DEFAULT_PAGE_SIZE,
        coroutineContext = sessionDispatcher,
    ) {
        innerRoomListService.allRooms()
    }
        get() = lastListWithSortOrder.get()?.first ?: field

    override val allSpaces: DynamicRoomList = roomListFactory.createRoomList(
        pageSize = DEFAULT_PAGE_SIZE,
        coroutineContext = sessionDispatcher,
    ) {
        innerRoomListService.allSpaces()
    }

    override val sortedRooms: DynamicRoomList
        get() = lastListWithSortOrder.get()?.first ?: allRooms

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
