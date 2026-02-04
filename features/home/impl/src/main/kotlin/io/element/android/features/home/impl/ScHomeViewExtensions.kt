package io.element.android.features.home.impl

import chat.schildi.features.home.spaces.SpaceListDataSource
import io.element.android.features.home.impl.roomlist.RoomListContentState

fun HomeState.scSelectedNavigationBarItem(): HomeNavigationBarItem {
    val selectedSpace = (roomListState.contentState as? RoomListContentState.Rooms)?.spaceSelectionHierarchy
    return if (selectedSpace?.size == 1 && selectedSpace.first() == SpaceListDataSource.UpstreamSpaceListItem.SPACE_ID) {
        HomeNavigationBarItem.Spaces
    } else {
        currentHomeNavigationBarItem
    }
}
