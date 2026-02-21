package chat.schildi.features.home

import chat.schildi.lib.preferences.ScPreferencesStore
import chat.schildi.lib.preferences.ScPrefs
import chat.schildi.lib.preferences.safeLookup
import io.element.android.libraries.matrix.api.roomlist.DynamicRoomList
import io.element.android.libraries.matrix.api.roomlist.RoomListFilter
import io.element.android.libraries.matrix.api.roomlist.ScSdkInboxSettings
import io.element.android.libraries.matrix.api.roomlist.ScSdkRoomSortOrder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ScInboxSettingsHandler(
    private val scPreferencesStore: ScPreferencesStore,
    private val roomList: DynamicRoomList,
) {
    fun launchIn(scope: CoroutineScope, roomListFilterFlow: Flow<RoomListFilter>) {
        scPreferencesStore.combinedSettingFlow { lookup ->
            ScSdkInboxSettings(
                sortOrder = ScSdkRoomSortOrder(
                    byUnread = ScPrefs.SORT_BY_UNREAD.safeLookup(lookup),
                    pinFavourites = ScPrefs.PIN_FAVORITES.safeLookup(lookup),
                    buryLowPriority = ScPrefs.BURY_LOW_PRIORITY.safeLookup(lookup),
                    clientSideUnreadCounts = ScPrefs.CLIENT_GENERATED_UNREAD_COUNTS.safeLookup(lookup),
                    withSilentUnread = ScPrefs.SORT_WITH_SILENT_UNREAD.safeLookup(lookup),
                )
            )
        }.combine(roomListFilterFlow) {a, b ->
            Pair(a, b)
        }.distinctUntilChanged().onEach { (inboxSettings, filter) ->
            roomList.updateSettings(filter, inboxSettings)
        }.launchIn(scope)
    }
}
