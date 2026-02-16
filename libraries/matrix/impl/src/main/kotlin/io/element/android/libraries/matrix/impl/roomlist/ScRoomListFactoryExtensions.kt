package io.element.android.libraries.matrix.impl.roomlist

import io.element.android.libraries.matrix.api.roomlist.ScSdkInboxSettings
import io.element.android.libraries.matrix.api.roomlist.ScSdkRoomSortOrder
import org.matrix.rustcomponents.sdk.RoomListEntriesDynamicFilterKind

fun ScSdkRoomSortOrder.toSdkSortOrder() = uniffi.matrix_sdk.ScSortOrder(
    byUnread = byUnread,
    pinFavorites = pinFavourites,
    buryLowPriority = buryLowPriority,
    clientGeneratedUnread = clientSideUnreadCounts,
    withSilentUnread = withSilentUnread,
)

fun ScSdkInboxSettings.toSdkSettings() = uniffi.matrix_sdk.ScInboxSettings(
    sortOrder = sortOrder.toSdkSortOrder(),
)
