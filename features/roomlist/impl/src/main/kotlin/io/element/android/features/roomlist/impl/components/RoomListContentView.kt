/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomlist.impl.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import chat.schildi.features.roomlist.spaces.SpacesPager
import chat.schildi.features.roomlist.spaces.isSpaceFilterActive
import chat.schildi.features.roomlist.spaces.resolveSelection
import chat.schildi.lib.preferences.ScPrefs
import chat.schildi.lib.preferences.value
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.roomlist.impl.R
import io.element.android.features.roomlist.impl.RoomListContentState
import io.element.android.features.roomlist.impl.RoomListContentStateProvider
import io.element.android.features.roomlist.impl.RoomListEvents
import io.element.android.features.roomlist.impl.SecurityBannerState
import io.element.android.features.roomlist.impl.contentType
import io.element.android.features.roomlist.impl.filters.RoomListFilter
import io.element.android.features.roomlist.impl.filters.RoomListFiltersEmptyStateResources
import io.element.android.features.roomlist.impl.filters.RoomListFiltersState
import io.element.android.features.roomlist.impl.filters.aRoomListFiltersState
import io.element.android.features.roomlist.impl.filters.selection.FilterSelectionState
import io.element.android.features.roomlist.impl.model.RoomListRoomSummary
import io.element.android.features.roomlist.impl.model.RoomSummaryDisplayType
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch

@Composable
fun RoomListContentView(
    contentState: RoomListContentState,
    filtersState: RoomListFiltersState,
    onMeasureSpaceBarHeight: (Int) -> Unit = {}, // SC
    eventSink: (RoomListEvents) -> Unit,
    onSetUpRecoveryClick: () -> Unit,
    onConfirmRecoveryKeyClick: () -> Unit,
    onRoomClick: (RoomListRoomSummary) -> Unit,
    onCreateRoomClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        when (contentState) {
            is RoomListContentState.Skeleton -> {
                SkeletonView(
                    count = contentState.count,
                )
            }
            is RoomListContentState.Empty -> {
                EmptyView(
                    state = contentState,
                    eventSink = eventSink,
                    onSetUpRecoveryClick = onSetUpRecoveryClick,
                    onConfirmRecoveryKeyClick = onConfirmRecoveryKeyClick,
                    onCreateRoomClick = onCreateRoomClick,
                )
            }
            is RoomListContentState.Rooms -> {
                RoomsView(
                    state = contentState,
                    filtersState = filtersState,
                    onMeasureSpaceBarHeight = onMeasureSpaceBarHeight, // SC
                    eventSink = eventSink,
                    onSetUpRecoveryClick = onSetUpRecoveryClick,
                    onConfirmRecoveryKeyClick = onConfirmRecoveryKeyClick,
                    onRoomClick = onRoomClick,
                )
            }
        }
    }
}

@Composable
private fun SkeletonView(count: Int, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier) {
        repeat(count) { index ->
            item {
                RoomSummaryPlaceholderRow()
                if (index != count - 1) {
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun EmptyView(
    state: RoomListContentState.Empty,
    eventSink: (RoomListEvents) -> Unit,
    onSetUpRecoveryClick: () -> Unit,
    onConfirmRecoveryKeyClick: () -> Unit,
    onCreateRoomClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize()) {
        EmptyScaffold(
            title = R.string.screen_roomlist_empty_title,
            subtitle = R.string.screen_roomlist_empty_message,
            action = {
                Button(
                    text = stringResource(CommonStrings.action_start_chat),
                    leadingIcon = IconSource.Vector(CompoundIcons.Compose()),
                    onClick = onCreateRoomClick,
                )
            },
            modifier = Modifier.align(Alignment.Center),
        )
        Box {
            when (state.securityBannerState) {
                SecurityBannerState.SetUpRecovery -> {
                    SetUpRecoveryKeyBanner(
                        onContinueClick = onSetUpRecoveryClick,
                        onDismissClick = { eventSink(RoomListEvents.DismissBanner) },
                    )
                }
                SecurityBannerState.RecoveryKeyConfirmation -> {
                    ConfirmRecoveryKeyBanner(
                        onContinueClick = onConfirmRecoveryKeyClick,
                        onDismissClick = { eventSink(RoomListEvents.DismissBanner) },
                    )
                }
                else -> Unit
            }
        }
    }
}

@Composable
private fun RoomsView(
    state: RoomListContentState.Rooms,
    filtersState: RoomListFiltersState,
    onMeasureSpaceBarHeight: (Int) -> Unit, // SC
    eventSink: (RoomListEvents) -> Unit,
    onSetUpRecoveryClick: () -> Unit,
    onConfirmRecoveryKeyClick: () -> Unit,
    onRoomClick: (RoomListRoomSummary) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.summaries.isEmpty() && (filtersState.hasAnyFilterSelected && state.spacesList.isEmpty())) {
        EmptyViewForFilterStates(
            selectedFilters = filtersState.selectedFilters(),
            modifier = modifier.fillMaxSize()
        )
    } else {
        RoomsViewList(
            state = state,
            filtersState = filtersState, // SC
            onMeasureSpaceBarHeight = onMeasureSpaceBarHeight, // SC
            eventSink = eventSink,
            onSetUpRecoveryClick = onSetUpRecoveryClick,
            onConfirmRecoveryKeyClick = onConfirmRecoveryKeyClick,
            onRoomClick = onRoomClick,
            modifier = modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun RoomsViewList(
    state: RoomListContentState.Rooms,
    filtersState: RoomListFiltersState, // SC
    onMeasureSpaceBarHeight: (Int) -> Unit, // SC
    eventSink: (RoomListEvents) -> Unit,
    onSetUpRecoveryClick: () -> Unit,
    onConfirmRecoveryKeyClick: () -> Unit,
    onRoomClick: (RoomListRoomSummary) -> Unit,
    modifier: Modifier = Modifier,
) {
    val withSpaceFilter = isSpaceFilterActive(state.spaceSelectionHierarchy)

    val lazyListState = rememberLazyListState()
    val visibleRange by remember {
        derivedStateOf {
            val layoutInfo = lazyListState.layoutInfo
            val firstItemIndex = layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0
            val size = layoutInfo.visibleItemsInfo.size
            firstItemIndex until firstItemIndex + size
        }
    }
    val updatedEventSink by rememberUpdatedState(newValue = eventSink)
    LaunchedEffect(visibleRange) {
        updatedEventSink(RoomListEvents.UpdateVisibleRange(visibleRange))
    }
    val coroutineScope = rememberCoroutineScope()
    SpacesPager(
        spacesList = state.spacesList,
        totalUnreadCounts = state.totalUnreadCounts,
        spaceSelectionHierarchy = state.spaceSelectionHierarchy,
        onSpaceSelected = { selection ->
            eventSink(RoomListEvents.UpdateSpaceFilter(selection))
            coroutineScope.launch { lazyListState.scrollToItem(0) }
        },
        onMeasureSpaceBarHeight = onMeasureSpaceBarHeight,
        modifier = modifier,
    ) { modifier ->
    LazyColumn(
        state = lazyListState,
        modifier = modifier,
        // FAB height is 56dp, bottom padding is 16dp, we add 8dp as extra margin -> 56+16+8 = 80
        contentPadding = PaddingValues(bottom = 80.dp).takeIf { ScPrefs.SNC_FAB.value() } ?: PaddingValues()
    ) {
        when (state.securityBannerState) {
            SecurityBannerState.SetUpRecovery -> {
                item {
                    SetUpRecoveryKeyBanner(
                        onContinueClick = onSetUpRecoveryClick,
                        onDismissClick = { updatedEventSink(RoomListEvents.DismissBanner) },
                    )
                }
            }
            SecurityBannerState.RecoveryKeyConfirmation -> {
                item {
                    ConfirmRecoveryKeyBanner(
                        onContinueClick = onConfirmRecoveryKeyClick,
                        onDismissClick = { updatedEventSink(RoomListEvents.DismissBanner) },
                    )
                }
            }
            SecurityBannerState.None -> if (state.fullScreenIntentPermissionsState.shouldDisplayBanner) {
                item {
                    FullScreenIntentPermissionBanner(state = state.fullScreenIntentPermissionsState)
                }
            }
        }

        // Note: do not use a key for the LazyColumn, or the scroll will not behave as expected if a room
        // is moved to the top of the list.
        itemsIndexed(
            items = state.summaries,
            contentType = { _, room -> room.contentType() },
        ) { index, room ->
            if (ScPrefs.SC_OVERVIEW_LAYOUT.value()) {
                ScRoomSummaryRow(
                    room = room,
                    isInviteSeen = room.displayType == RoomSummaryDisplayType.INVITE &&
                        state.seenRoomInvites.contains(room.roomId),
                    onClick = onRoomClick,
                    eventSink = eventSink,
                    isLastIndex = index == state.summaries.lastIndex,
                )
                return@itemsIndexed
            }
            RoomSummaryRow(
                room = room,
                isInviteSeen = room.displayType == RoomSummaryDisplayType.INVITE &&
                    state.seenRoomInvites.contains(room.roomId),
                onClick = onRoomClick,
                eventSink = eventSink,
            )
            if (index != state.summaries.lastIndex) {
                HorizontalDivider()
            }
        }
    }
        // SC empty space view
        if (state.summaries.isEmpty()) {
            if (filtersState.hasAnyFilterSelected)
                EmptyViewForFilterStates(filtersState.selectedFilters(), Modifier.fillMaxSize())
            else
                ScSpaceEmptyView(state.spacesList.resolveSelection(state.spaceSelectionHierarchy))
        }
    }
}

@Composable
private fun EmptyViewForFilterStates(
    selectedFilters: ImmutableList<RoomListFilter>,
    modifier: Modifier = Modifier,
) {
    val emptyStateResources = RoomListFiltersEmptyStateResources.fromSelectedFilters(selectedFilters) ?: return
    EmptyScaffold(
        title = emptyStateResources.title,
        subtitle = emptyStateResources.subtitle,
        modifier = modifier,
    )
}

@Composable
private fun EmptyScaffold(
    @StringRes title: Int,
    @StringRes subtitle: Int,
    modifier: Modifier = Modifier,
    action: @Composable (ColumnScope.() -> Unit)? = null,
) {
    Column(
        modifier = modifier.padding(horizontal = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(title),
            style = ElementTheme.typography.fontHeadingMdBold,
            color = ElementTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(subtitle),
            style = ElementTheme.typography.fontBodyLgRegular,
            color = ElementTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(32.dp))
        action?.invoke(this)
    }
}

@PreviewsDayNight
@Composable
internal fun RoomListContentViewPreview(@PreviewParameter(RoomListContentStateProvider::class) state: RoomListContentState) = ElementPreview {
    RoomListContentView(
        contentState = state,
        filtersState = aRoomListFiltersState(
            filterSelectionStates = RoomListFilter.entries.map { FilterSelectionState(it, isSelected = true) }
        ),
        eventSink = {},
        onSetUpRecoveryClick = {},
        onConfirmRecoveryKeyClick = {},
        onRoomClick = {},
        onCreateRoomClick = {},
    )
}
