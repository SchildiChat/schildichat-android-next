package io.element.android.features.home.impl

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import chat.schildi.features.home.spaces.SpaceListDataSource
import chat.schildi.features.home.spaces.resolveSpaceName
import chat.schildi.lib.preferences.ScPrefs
import chat.schildi.lib.preferences.value
import io.element.android.features.home.impl.roomlist.RoomListContentState
import io.element.android.features.home.impl.roomlist.RoomListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun scRoomListScrollBehavior(): TopAppBarScrollBehavior? {
    return if (ScPrefs.ELEMENT_ROOM_LIST_FILTERS.value())
        null
    else
        TopAppBarDefaults.pinnedScrollBehavior()
}

@Composable
fun RoomListState.resolveSpaceName() = (this.contentState as? RoomListContentState.Rooms)?.let { it.spacesList.resolveSpaceName(it.spaceSelectionHierarchy) }

fun RoomListState.isShowingUpstreamSpaceList() = (this.contentState as? RoomListContentState.Rooms)?.let {
    it.spaceSelectionHierarchy.firstOrNull() == SpaceListDataSource.UpstreamSpaceListItem.SPACE_ID
} == true

@Composable
fun Modifier.addSpaceNavPadding(spaceBarHeight: Int): Modifier =
    if (ScPrefs.SPACE_NAV.value()) {
        val lastWasZero = remember { mutableStateOf(true) }
        val targetPadding = if (lastWasZero.value) {
            spaceBarHeight
        } else {
            animateIntAsState(spaceBarHeight, label = "sncFabSpaceBarPadding").value
        }
        lastWasZero.value = spaceBarHeight == 0
        val extraPadding = LocalDensity.current.run { targetPadding.toDp() }
        padding(bottom = extraPadding)
    } else {
        this
    }
