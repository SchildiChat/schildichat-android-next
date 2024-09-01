package chat.schildi.features.roomlist

import androidx.lifecycle.AtomicReference
import chat.schildi.lib.preferences.ScPreferencesStore
import chat.schildi.lib.preferences.ScPrefs
import io.element.android.libraries.core.coroutine.childScope
import io.element.android.libraries.matrix.api.roomlist.RoomList
import io.element.android.libraries.matrix.api.roomlist.RoomListFilter
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import io.element.android.libraries.matrix.api.roomlist.ScRoomSortOrder
import io.element.android.libraries.matrix.api.roomlist.loadAllIncrementally
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import timber.log.Timber
import javax.inject.Inject

class ScRoomSortOrderSource @Inject constructor(
    private val scPreferencesStore: ScPreferencesStore,
    private val roomListService: RoomListService,
) {
    private val lastCoroutineScope = AtomicReference<CoroutineScope?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    fun filteredSummaries(coroutineScope: CoroutineScope): Flow<List<RoomSummary>> {
        // Listen to preferences relevant to sort order, then apply these to the dynamic room list
        return combine(
            scPreferencesStore.settingFlow(ScPrefs.SORT_BY_UNREAD),
            scPreferencesStore.settingFlow(ScPrefs.PIN_FAVORITES),
            scPreferencesStore.settingFlow(ScPrefs.BURY_LOW_PRIORITY),
            scPreferencesStore.settingFlow(ScPrefs.CLIENT_GENERATED_UNREAD_COUNTS),
        ) { byUnread, pinFavorites, buryLowPriority, clientSideUnreadCounts ->
            ScRoomSortOrder(
                byUnread = byUnread,
                pinFavourites = pinFavorites,
                buryLowPriority = buryLowPriority,
                clientSideUnreadCounts = clientSideUnreadCounts,
            )
        }.flatMapLatest { sortOrder ->
            // TODO: would be nice to teach the SDK to update sort order without recreating the whole list
            // Cancel jobs for previous list
            val scope = coroutineScope.childScope(Dispatchers.Default, "sc-sorted-room-list")
            lastCoroutineScope.getAndSet(scope)?.cancel()
            Timber.d("Create new filtered and sorted room list for $sortOrder")
            roomListService.scCreateRoomList(
                pageSize = 30,
                initialFilter = RoomListFilter.all(),
                source = RoomList.Source.All,
                sortOrder = sortOrder,
                coroutineScope = scope,
            ).also {
                it.loadAllIncrementally(scope)
            }.filteredSummaries
        }
    }
}
