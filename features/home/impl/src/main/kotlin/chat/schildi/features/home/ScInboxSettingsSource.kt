package chat.schildi.features.home

import chat.schildi.lib.preferences.ScPreferencesStore
import chat.schildi.lib.preferences.ScPrefs
import chat.schildi.lib.preferences.safeLookup
import dev.zacsweers.metro.Inject
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.api.roomlist.ScSdkInboxSettings
import io.element.android.libraries.matrix.api.roomlist.ScSdkRoomSortOrder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Inject
class ScInboxSettingsSource(
    private val scPreferencesStore: ScPreferencesStore,
    private val roomListService: RoomListService,
) {
    fun launchIn(scope: CoroutineScope) {
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
        }.onEach { inboxSettings ->
            roomListService.allRooms.updateSettings(inboxSettings)
        }.launchIn(scope)
    }
}
