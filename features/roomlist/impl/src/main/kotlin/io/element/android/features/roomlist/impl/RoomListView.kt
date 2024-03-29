/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.roomlist.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import chat.schildi.lib.compose.thenIf
import chat.schildi.lib.preferences.ScPrefs
import chat.schildi.lib.preferences.value
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.leaveroom.api.LeaveRoomView
import io.element.android.features.networkmonitor.api.ui.ConnectivityIndicatorContainer
import io.element.android.features.roomlist.impl.components.RoomListContentView
import io.element.android.features.roomlist.impl.components.RoomListMenuAction
import io.element.android.features.roomlist.impl.components.RoomListTopBar
import io.element.android.features.roomlist.impl.model.RoomListRoomSummary
import io.element.android.features.roomlist.impl.search.RoomListSearchView
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.FloatingActionButton
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarHost
import io.element.android.libraries.designsystem.utils.snackbar.rememberSnackbarHostState
import io.element.android.libraries.matrix.api.core.RoomId

@Composable
fun RoomListView(
    state: RoomListState,
    onRoomClicked: (RoomId) -> Unit,
    onSettingsClicked: () -> Unit,
    onVerifyClicked: () -> Unit,
    onConfirmRecoveryKeyClicked: () -> Unit,
    onCreateRoomClicked: () -> Unit,
    onInvitesClicked: () -> Unit,
    onRoomSettingsClicked: (roomId: RoomId) -> Unit,
    onMenuActionClicked: (RoomListMenuAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    ConnectivityIndicatorContainer(
        modifier = modifier,
        isOnline = state.hasNetworkConnection,
    ) { topPadding ->
        Box {
            fun onRoomLongClicked(
                roomListRoomSummary: RoomListRoomSummary
            ) {
                state.eventSink(RoomListEvents.ShowContextMenu(roomListRoomSummary))
            }

            if (state.contextMenu is RoomListState.ContextMenu.Shown) {
                RoomListContextMenu(
                    contextMenu = state.contextMenu,
                    eventSink = state.eventSink,
                    onRoomSettingsClicked = onRoomSettingsClicked,
                )
            }

            LeaveRoomView(state = state.leaveRoomState)

            RoomListScaffold(
                modifier = Modifier.padding(top = topPadding),
                state = state,
                onVerifyClicked = onVerifyClicked,
                onConfirmRecoveryKeyClicked = onConfirmRecoveryKeyClicked,
                onRoomClicked = onRoomClicked,
                onRoomLongClicked = { onRoomLongClicked(it) },
                onOpenSettings = onSettingsClicked,
                onCreateRoomClicked = onCreateRoomClicked,
                onInvitesClicked = onInvitesClicked,
                onMenuActionClicked = onMenuActionClicked,
            )
            // This overlaid view will only be visible when state.displaySearchResults is true
            RoomListSearchView(
                state = state.searchState,
                onRoomClicked = onRoomClicked,
                onRoomLongClicked = { onRoomLongClicked(it) },
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = topPadding)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoomListScaffold(
    state: RoomListState,
    onVerifyClicked: () -> Unit,
    onConfirmRecoveryKeyClicked: () -> Unit,
    onRoomClicked: (RoomId) -> Unit,
    onRoomLongClicked: (RoomListRoomSummary) -> Unit,
    onOpenSettings: () -> Unit,
    onCreateRoomClicked: () -> Unit,
    onInvitesClicked: () -> Unit,
    onMenuActionClicked: (RoomListMenuAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    fun onRoomClicked(room: RoomListRoomSummary) {
        onRoomClicked(room.roomId)
    }

    val appBarState = rememberTopAppBarState()
    val scrollBehavior = scRoomListScrollBehavior(appBarState)
    val snackbarHostState = rememberSnackbarHostState(snackbarMessage = state.snackbarMessage)

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            // SC: clear cache / setting up your account looks bad with top app bar showing
            if (state.contentState is RoomListContentState.Migration) return@Scaffold

            RoomListTopBar(
                matrixUser = state.matrixUser,
                showAvatarIndicator = state.showAvatarIndicator,
                areSearchResultsDisplayed = state.searchState.isSearchActive,
                // SC start
                selectedSpaceName = state.resolveSpaceName(),
                onCreateRoomClicked = onCreateRoomClicked,
                // SC end
                onToggleSearch = { state.eventSink(RoomListEvents.ToggleSearchResults) },
                onMenuActionClicked = onMenuActionClicked,
                onOpenSettings = onOpenSettings,
                scrollBehavior = scrollBehavior,
                displayMenuItems = state.displayActions,
                displayFilters = state.displayFilters,
                filtersState = state.filtersState,
            )
        },
        content = { padding ->
            RoomListContentView(
                contentState = state.contentState,
                filtersState = state.filtersState,
                eventSink = state.eventSink,
                onVerifyClicked = onVerifyClicked,
                onConfirmRecoveryKeyClicked = onConfirmRecoveryKeyClicked,
                onRoomClicked = ::onRoomClicked,
                onRoomLongClicked = onRoomLongClicked,
                onCreateRoomClicked = onCreateRoomClicked,
                onInvitesClicked = onInvitesClicked,
                modifier = Modifier
                    // SC: go to edge for migration screen
                    .thenIf(state.contentState !is RoomListContentState.Migration) { this
                    .padding(padding)
                    .consumeWindowInsets(padding)
                    }
            )
        },
        floatingActionButton = {
            if (state.displayActions && !ScPrefs.SPACE_NAV.value()) {
                FloatingActionButton(
                    // FIXME align on Design system theme
                    containerColor = MaterialTheme.colorScheme.primary,
                    onClick = onCreateRoomClicked
                ) {
                    Icon(
                        // Note cannot use Icons.Outlined.EditSquare, it does not exist :/
                        imageVector = CompoundIcons.Compose(),
                        contentDescription = stringResource(id = R.string.screen_roomlist_a11y_create_message)
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    )
}

internal fun RoomListRoomSummary.contentType() = isPlaceholder

@PreviewsDayNight
@Composable
internal fun RoomListViewPreview(@PreviewParameter(RoomListStateProvider::class) state: RoomListState) = ElementPreview {
    RoomListView(
        state = state,
        onRoomClicked = {},
        onSettingsClicked = {},
        onVerifyClicked = {},
        onConfirmRecoveryKeyClicked = {},
        onCreateRoomClicked = {},
        onInvitesClicked = {},
        onRoomSettingsClicked = {},
        onMenuActionClicked = {},
    )
}
