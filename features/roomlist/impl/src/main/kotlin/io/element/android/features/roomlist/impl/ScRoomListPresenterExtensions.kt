package io.element.android.features.roomlist.impl

import androidx.compose.runtime.MutableState
import io.element.android.libraries.matrix.api.room.MatrixRoom
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile

@OptIn(ExperimentalCoroutinesApi::class)
internal fun CoroutineScope.handleLowPriorityFlow(
    room: MatrixRoom,
    contextMenuState: MutableState<RoomListState.ContextMenu>,
    initialState: RoomListState.ContextMenu.Shown,
    isShowingContextMenuFlow: Flow<Boolean>,
) {
    val isLowPriorityFlow = room.roomInfoFlow
        .map { it.isLowPriority }
        .distinctUntilChanged()

    isLowPriorityFlow
        .onEach { isLowPriority ->
            contextMenuState.value = (contextMenuState.value as? RoomListState.ContextMenu.Shown ?: initialState).copy(isLowPriority = isLowPriority)
        }
        .flatMapLatest { isShowingContextMenuFlow }
        .takeWhile { isShowingContextMenu -> isShowingContextMenu }
        .launchIn(this)
}
