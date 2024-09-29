package chat.schildi.features.roomlist

import chat.schildi.lib.preferences.ScPreferencesStore
import chat.schildi.lib.preferences.ScPrefs
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.api.roomlist.ScSdkInboxSettings
import io.element.android.libraries.matrix.api.roomlist.ScSdkRoomSortOrder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class ScInboxSettingsSource @Inject constructor(
    private val scPreferencesStore: ScPreferencesStore,
    private val roomListService: RoomListService,
) {
    fun launchIn(scope: CoroutineScope) {
        scPreferencesStore.combinedSettingFlow { lookup ->
            ScSdkInboxSettings(
                sortOrder = ScSdkRoomSortOrder(
                    byUnread = ScPrefs.SORT_BY_UNREAD.let { it.ensureType(lookup(it)) ?: it.defaultValue },
                    pinFavourites = ScPrefs.PIN_FAVORITES.let { it.ensureType(lookup(it)) ?: it.defaultValue },
                    buryLowPriority = ScPrefs.BURY_LOW_PRIORITY.let { it.ensureType(lookup(it)) ?: it.defaultValue },
                    clientSideUnreadCounts = ScPrefs.CLIENT_GENERATED_UNREAD_COUNTS.let { it.ensureType(lookup(it)) ?: it.defaultValue },
                    withSilentUnread = ScPrefs.SORT_WITH_SILENT_UNREAD.let { it.ensureType(lookup(it)) ?: it.defaultValue },
                )
            )
        }.onEach { inboxSettings ->
            roomListService.allRooms.updateSettings(inboxSettings)
        }.launchIn(scope)
    }
}
