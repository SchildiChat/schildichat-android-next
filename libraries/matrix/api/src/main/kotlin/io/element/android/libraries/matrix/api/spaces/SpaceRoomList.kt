/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.spaces

import io.element.android.libraries.matrix.api.core.RoomId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.Optional

interface SpaceRoomList {
    sealed interface PaginationStatus {
        data object Loading : PaginationStatus
        data class Idle(val hasMoreToLoad: Boolean) : PaginationStatus
    }

    val spaceId: RoomId

    val currentSpaceFlow: StateFlow<Optional<SpaceRoom>>

    val spaceRoomsFlow: Flow<List<SpaceRoom>>
    val paginationStatusFlow: StateFlow<PaginationStatus>

    suspend fun paginate(): Result<Unit>
    suspend fun reset(): Result<Unit>

    fun destroy()
}

/**
 * Loads all space rooms incrementally by automatically paginating whenever more data is available.
 * This function observes the pagination status and triggers [paginate] calls until the entire list is loaded.
 *
 * @param coroutineScope The scope in which the pagination flow will be collected.
 */
fun SpaceRoomList.loadAllIncrementally(coroutineScope: CoroutineScope) {
    paginationStatusFlow
        .onEach { paginationStatus ->
            if (paginationStatus is SpaceRoomList.PaginationStatus.Idle && paginationStatus.hasMoreToLoad) {
                paginate()
            }
        }
        .launchIn(coroutineScope)
}
