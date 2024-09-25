package io.element.android.libraries.matrix.impl.roomlist

import io.element.android.libraries.matrix.api.roomlist.ScRoomSortOrder

fun ScRoomSortOrder.toSdkSortOrder() = uniffi.matrix_sdk_ui.ScSortOrder(
        byUnread = byUnread,
        pinFavorites = pinFavourites,
        buryLowPriority = buryLowPriority,
        clientGeneratedUnread = clientSideUnreadCounts,
        withSilentUnread = withSilentUnread,
    )
