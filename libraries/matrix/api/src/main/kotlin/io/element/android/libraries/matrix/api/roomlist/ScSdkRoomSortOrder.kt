package io.element.android.libraries.matrix.api.roomlist

import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import kotlin.math.sign

data class ScSdkInboxSettings(
    val sortOrder: ScSdkRoomSortOrder = ScSdkRoomSortOrder(),
)

data class ScSdkRoomSortOrder(
    val byUnread: Boolean = false,
    val pinFavourites: Boolean = false,
    val buryLowPriority: Boolean = false,
    val clientSideUnreadCounts: Boolean = false,
    val withSilentUnread: Boolean = false,
) {
    fun <T>toComparator(map: (T) -> RoomSummary) = Comparator<T> { ua, ub ->
        val a = map(ua)
        val b = map(ub)
        // Invites on top
        selectFor(a, b) { it.info.currentUserMembership == CurrentUserMembership.INVITED }?.let { return@Comparator it }
        if (pinFavourites) {
            selectFor(a, b) { it.info.isFavorite }?.let { return@Comparator it }
        }
        if (buryLowPriority) {
            selectFor(a, b) { it.info.isLowPriority }?.let { return@Comparator -it }
        }
        if (byUnread) {
            selectFor(a, b) { it.info.isMarkedUnread || it.info.numUnreadNotifications > 0 }?.let { return@Comparator it }
            if (withSilentUnread) {
                selectFor(a, b) { it.info.numUnreadMessages > 0 }?.let { return@Comparator it }
            }
        }
        selectFor(a, b) { it.latestEventTimestamp != null }?.let { return@Comparator it }
        ((b.latestEventTimestamp ?: 0L) - (a.latestEventTimestamp ?: 0L)).sign
    }
}

private fun <T>selectFor(a: T, b: T, map: (T) -> Boolean): Int? {
    val va = map(a)
    val vb = map(b)
    return if (va != vb) {
        if (va) {
            -1
        } else {
            1
        }
    } else {
        null
    }
}
