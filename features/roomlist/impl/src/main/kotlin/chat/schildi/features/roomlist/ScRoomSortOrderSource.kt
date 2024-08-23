package chat.schildi.features.roomlist

import chat.schildi.lib.preferences.ScPreferencesStore
import chat.schildi.lib.preferences.ScPrefs
import io.element.android.features.roomlist.impl.model.RoomListRoomSummary
import io.element.android.features.roomlist.impl.model.RoomSummaryDisplayType
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
            scPreferencesStore.settingFlow(ScPrefs.BURY_LOW_PRIORITY),
            scPreferencesStore.settingFlow(ScPrefs.CLIENT_SIDE_SORT),
            scPreferencesStore.settingFlow(ScPrefs.SORT_BY_ACTIVITY),
            scPreferencesStore.settingFlow(ScPrefs.CLIENT_GENERATED_UNREAD_COUNTS),
        ) { pinFavorites, buryLowPriority, clientSideSort, activitySort, clientSideUnreadCounts ->
            ScRoomSortOrder(pinFavorites, buryLowPriority, clientSideSort, activitySort, clientSideUnreadCounts)
        }.onEach { result ->
            _sortOrder.emit(result)
        }.launchIn(coroutineScope)
    }

    fun sortRooms(rooms: List<RoomListRoomSummary>, order: ScRoomSortOrder): List<RoomListRoomSummary> {
        // TODO: move to SDK?
        return if (order.needsAction()) {
            // Do activity-based sorting as separate step, since we do not know for sure the range of timestamps,
            // but we want to prioritize favorite state above activity
            if (order.activitySort) {
                rooms.sortedByDescending { it.lastMessageTimestamp ?: 0L }
            } else {
                rooms
            }.sortedBy { room ->
                val inviteAdd = if (room.displayType == RoomSummaryDisplayType.INVITE) -10_000 else 0
                val favoriteAdd = if (order.pinFavorites && !room.isFavorite) 1000 else 0
                val lowPrioAdd = if (order.buryLowPriority && room.isLowPriority) 100 else 0
                val unreadAdd = when {
                    order.activitySort -> 0
                    !order.clientSideSort -> 0
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
                inviteAdd + favoriteAdd + lowPrioAdd + unreadAdd
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
    val buryLowPriority: Boolean,
    val clientSideSort: Boolean,
    val activitySort: Boolean,
    val clientSideUnreadCounts: Boolean,
) {
    fun needsAction() = pinFavorites || buryLowPriority || clientSideSort || activitySort
}
