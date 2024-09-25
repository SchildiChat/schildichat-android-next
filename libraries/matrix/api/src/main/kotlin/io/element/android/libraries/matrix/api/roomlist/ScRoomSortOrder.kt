package io.element.android.libraries.matrix.api.roomlist


data class ScRoomSortOrder(
    val byUnread: Boolean = false,
    val pinFavourites: Boolean = false,
    val buryLowPriority: Boolean = false,
    val clientSideUnreadCounts: Boolean = false,
    val withSilentUnread: Boolean = false,
)
