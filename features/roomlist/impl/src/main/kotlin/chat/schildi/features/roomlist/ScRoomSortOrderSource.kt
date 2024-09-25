package chat.schildi.features.roomlist

import chat.schildi.lib.preferences.ScPreferencesStore
import chat.schildi.lib.preferences.ScPrefs
import io.element.android.libraries.matrix.api.roomlist.RoomList
import io.element.android.libraries.matrix.api.roomlist.RoomListFilter
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import io.element.android.libraries.matrix.api.roomlist.ScRoomSortOrder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import timber.log.Timber
import javax.inject.Inject

class ScRoomSortOrderSource @Inject constructor(
    private val scPreferencesStore: ScPreferencesStore,
    private val roomListService: RoomListService,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun filteredSummaries(): Flow<List<RoomSummary>> {
        // Listen to preferences relevant to sort order, then apply these to the dynamic room list
        return scPreferencesStore.combinedSettingFlow { lookup ->
            ScRoomSortOrder(
                byUnread = ScPrefs.SORT_BY_UNREAD.let { it.ensureType(lookup(it)) ?: it.defaultValue },
                pinFavourites = ScPrefs.PIN_FAVORITES.let { it.ensureType(lookup(it)) ?: it.defaultValue },
                buryLowPriority = ScPrefs.BURY_LOW_PRIORITY.let { it.ensureType(lookup(it)) ?: it.defaultValue },
                clientSideUnreadCounts = ScPrefs.CLIENT_GENERATED_UNREAD_COUNTS.let { it.ensureType(lookup(it)) ?: it.defaultValue },
                withSilentUnread = ScPrefs.SORT_WITH_SILENT_UNREAD.let { it.ensureType(lookup(it)) ?: it.defaultValue },
            )
        }.flatMapLatest { sortOrder ->
            // TODO: would be nice to teach the SDK to update sort order without recreating the whole list
            // Cancel jobs for previous list
            Timber.d("Request sorted room list for $sortOrder")
            roomListService.getOrReplaceRoomListWithSortOrder(
                pageSize = 30,
                initialFilter = RoomListFilter.all(),
                sortOrder = sortOrder,
            ).filteredSummaries
        }
    }
}
