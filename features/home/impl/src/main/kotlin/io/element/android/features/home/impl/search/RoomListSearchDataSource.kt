/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.search

import chat.schildi.lib.preferences.ScPreferencesStore
import io.element.android.features.home.impl.datasource.applyInviteFilterSetting
import io.element.android.features.home.impl.datasource.RoomListRoomSummaryFactory
import io.element.android.features.home.impl.model.RoomListRoomSummary
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.matrix.api.roomlist.RoomList
import io.element.android.libraries.matrix.api.roomlist.RoomListFilter
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.api.roomlist.loadAllIncrementally
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private const val PAGE_SIZE = 30

class RoomListSearchDataSource @Inject constructor(
    roomListService: RoomListService,
    coroutineDispatchers: CoroutineDispatchers,
    scPreferencesStore: ScPreferencesStore,
    private val roomSummaryFactory: RoomListRoomSummaryFactory,
) {
    private val roomList = roomListService.createRoomList(
        pageSize = PAGE_SIZE,
        initialFilter = RoomListFilter.None,
        source = RoomList.Source.All,
    )

    val roomSummaries/*: Flow<PersistentList<RoomListRoomSummary>>*/ = roomList.filteredSummaries
        .map { roomSummaries ->
            roomSummaries
                .map(roomSummaryFactory::create)
                .toPersistentList()
        }
        .applyInviteFilterSetting(scPreferencesStore)
        .flowOn(coroutineDispatchers.computation)

    suspend fun setIsActive(isActive: Boolean) = coroutineScope {
        if (isActive) {
            roomList.loadAllIncrementally(this)
        } else {
            roomList.reset()
        }
    }

    suspend fun setSearchQuery(searchQuery: String) = coroutineScope {
        val filter = if (searchQuery.isBlank()) {
            RoomListFilter.None
        } else {
            RoomListFilter.NormalizedMatchRoomName(searchQuery)
        }
        roomList.updateFilter(filter)
    }
}
