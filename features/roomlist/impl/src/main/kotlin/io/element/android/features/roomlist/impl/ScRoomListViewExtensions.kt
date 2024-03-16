package io.element.android.features.roomlist.impl

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.Composable
import chat.schildi.features.roomlist.spaces.resolveSpaceName
import chat.schildi.lib.preferences.ScPrefs
import chat.schildi.lib.preferences.value

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun scRoomListScrollBehavior(appBarState: TopAppBarState): TopAppBarScrollBehavior {
    return if (ScPrefs.COMPACT_APP_BAR.value())
        TopAppBarDefaults.pinnedScrollBehavior()
    else
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(appBarState)
}

@Composable
fun RoomListState.resolveSpaceName() = (this.contentState as? RoomListContentState.Rooms)?.let { it.spacesList.resolveSpaceName(it.spaceSelectionHierarchy) }
