package chat.schildi.features.roomlist

import chat.schildi.lib.preferences.ScPreferencesStore
import chat.schildi.lib.preferences.ScPrefs
import io.element.android.features.roomlist.impl.model.RoomListRoomSummary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class ScRoomSortOrderSource @Inject constructor(
    private val scPreferencesStore: ScPreferencesStore
) {

    private val _sortOrder = MutableSharedFlow<ScRoomSortOrder>(replay = 1)
    val sortOrder: SharedFlow<ScRoomSortOrder> = _sortOrder

    fun launchIn(coroutineScope: CoroutineScope) {
        // From life space list and current space selection, build the RoomId filter
        combine(
            scPreferencesStore.settingFlow(ScPrefs.PIN_FAVORITES),
            scPreferencesStore.settingFlow(ScPrefs.CLIENT_SIDE_UNREAD_SORT),
            scPreferencesStore.settingFlow(ScPrefs.CLIENT_GENERATED_UNREAD_COUNTS),
        ) { pinFavorites, clientSideUnreadSort, clientSideUnreadCounts ->
            ScRoomSortOrder(pinFavorites, clientSideUnreadSort, clientSideUnreadCounts)
        }.onEach { result ->
            _sortOrder.emit(result)
        }.launchIn(coroutineScope)
    }

    fun sortRooms(rooms: List<RoomListRoomSummary>, order: ScRoomSortOrder): List<RoomListRoomSummary> {
        return if (order.needsAction()) {
            rooms.sortedBy { room ->
                val favoriteAdd = if (order.pinFavorites && !room.isFavorite) 100 else 0
                val unreadAdd = when {
                    !order.clientSideUnreadSort -> 0
                    order.clientSideUnreadCounts -> unreadSort(
                        room.isMarkedUnread,
                        room.numberOfUnreadMentions,
                        room.numberOfUnreadNotifications,
                        room.numberOfUnreadMessages
                    )
                    else -> unreadSort(
                        room.isMarkedUnread,
                        room.highlightCount,
                        room.notificationCount,
                        room.unreadCount
                    )
                }
                favoriteAdd + unreadAdd
            }
        } else {
            rooms
        }
    }

    private fun unreadSort(markedUnread: Boolean, mentionCount: Int, notificationCount: Int, unreadCount: Int): Int {
        return when {
            markedUnread || notificationCount > 0 || mentionCount > 0 -> 0
            unreadCount > 0 -> 1
            else -> 2
        }
    }
}

data class ScRoomSortOrder(
    val pinFavorites: Boolean,
    val clientSideUnreadSort: Boolean,
    val clientSideUnreadCounts: Boolean,
) {
    fun needsAction() = pinFavorites || clientSideUnreadSort
}
