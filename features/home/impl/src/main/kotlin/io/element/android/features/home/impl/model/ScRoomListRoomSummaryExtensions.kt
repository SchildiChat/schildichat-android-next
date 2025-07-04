package io.element.android.features.home.impl.model

import androidx.compose.runtime.Composable
import chat.schildi.lib.preferences.ScPreferencesStore
import chat.schildi.lib.preferences.ScPrefs
import chat.schildi.lib.preferences.value

private fun RoomListRoomSummary.hasNewContent(includeSilentUnread: Boolean, clientGeneratedUnread: Boolean) = when {
    isMarkedUnread || displayType == RoomSummaryDisplayType.INVITE -> true
    clientGeneratedUnread -> (includeSilentUnread && numberOfUnreadMessages > 0) || numberOfUnreadMentions > 0 || numberOfUnreadNotifications > 0
    else ->  (includeSilentUnread && unreadCount > 0) || highlightCount > 0 || notificationCount > 0
}

fun RoomListRoomSummary.hasNewContent(scPreferencesStore: ScPreferencesStore) = hasNewContent(
    scPreferencesStore.getCachedOrDefaultValue(ScPrefs.RENDER_SILENT_UNREAD),
    scPreferencesStore.getCachedOrDefaultValue(ScPrefs.CLIENT_GENERATED_UNREAD_COUNTS),
)

val RoomListRoomSummary.hasNewContent
    @Composable
    get() = hasNewContent(ScPrefs.RENDER_SILENT_UNREAD.value(), ScPrefs.CLIENT_GENERATED_UNREAD_COUNTS.value())
