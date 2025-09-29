/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl.root

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.components.async.AsyncIndicator
import io.element.android.libraries.designsystem.components.async.AsyncIndicatorHost
import io.element.android.libraries.designsystem.components.async.rememberAsyncIndicatorState
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.ButtonSize
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.DropdownMenu
import io.element.android.libraries.designsystem.theme.components.DropdownMenuItem
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import io.element.android.libraries.matrix.ui.components.SpaceHeaderView
import io.element.android.libraries.matrix.ui.components.SpaceRoomItemView
import io.element.android.libraries.matrix.ui.model.getAvatarData
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay

@Composable
fun SpaceView(
    state: SpaceState,
    onBackClick: () -> Unit,
    onRoomClick: (spaceRoom: SpaceRoom) -> Unit,
    onShareSpace: () -> Unit,
    onLeaveSpaceClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            SpaceViewTopBar(
                currentSpace = state.currentSpace,
                onBackClick = onBackClick,
                onLeaveSpaceClick = onLeaveSpaceClick,
                onShareSpace = onShareSpace,
            )
        },
        content = { padding ->
            Box(
                modifier = Modifier.padding(padding)
            ) {
                SpaceViewContent(
                    state = state,
                    onRoomClick = onRoomClick
                )
                JoinRoomFailureEffect(
                    hasAnyFailure = state.hasAnyFailure,
                    eventSink = state.eventSink
                )
            }
        },
    )
}

@Composable
private fun JoinRoomFailureEffect(
    hasAnyFailure: Boolean,
    eventSink: (SpaceEvents) -> Unit,
) {
    val asyncIndicatorState = rememberAsyncIndicatorState()
    val updatedEventSink by rememberUpdatedState(eventSink)
    AsyncIndicatorHost(modifier = Modifier, asyncIndicatorState)
    LaunchedEffect(hasAnyFailure) {
        if (hasAnyFailure) {
            asyncIndicatorState.enqueue {
                AsyncIndicator.Failure(text = stringResource(CommonStrings.common_something_went_wrong))
            }
            delay(AsyncIndicator.DURATION_SHORT)
            updatedEventSink(SpaceEvents.ClearFailures)
        } else {
            asyncIndicatorState.clear()
        }
    }
}

@Composable
private fun SpaceViewContent(
    state: SpaceState,
    onRoomClick: (spaceRoom: SpaceRoom) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier.fillMaxSize()) {
        val currentSpace = state.currentSpace
        if (currentSpace != null) {
            item {
                SpaceHeaderView(
                    avatarData = currentSpace.getAvatarData(AvatarSize.SpaceHeader),
                    name = currentSpace.name,
                    topic = currentSpace.topic,
                    joinRule = currentSpace.joinRule,
                    heroes = currentSpace.heroes.toImmutableList(),
                    numberOfMembers = currentSpace.numJoinedMembers,
                    numberOfRooms = currentSpace.childrenCount,
                )
            }
        }
        state.children.forEach { spaceRoom ->
            item {
                val isInvitation = spaceRoom.state == CurrentUserMembership.INVITED
                val isCurrentlyJoining = state.isJoining(spaceRoom.roomId)
                SpaceRoomItemView(
                    spaceRoom = spaceRoom,
                    showUnreadIndicator = isInvitation && spaceRoom.roomId !in state.seenSpaceInvites,
                    hideAvatars = isInvitation && state.hideInvitesAvatar,
                    onClick = {
                        onRoomClick(spaceRoom)
                    },
                    onLongClick = {
                        // TODO
                    },
                    trailingAction = spaceRoom.trailingAction(isCurrentlyJoining = isCurrentlyJoining) {
                        state.eventSink(SpaceEvents.Join(spaceRoom))
                    }
                )
            }
        }
        if (state.hasMoreToLoad) {
            item {
                LoadingMoreIndicator(eventSink = state.eventSink)
            }
        }
    }
}

@Composable
private fun LoadingMoreIndicator(
    eventSink: (SpaceEvents) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            strokeWidth = 2.dp,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        val latestEventSink by rememberUpdatedState(eventSink)
        LaunchedEffect(Unit) {
            latestEventSink(SpaceEvents.LoadMore)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SpaceViewTopBar(
    currentSpace: SpaceRoom?,
    onBackClick: () -> Unit,
    @Suppress("unused") onLeaveSpaceClick: () -> Unit,
    onShareSpace: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        navigationIcon = {
            BackButton(onClick = onBackClick)
        },
        title = {
            if (currentSpace != null) {
                SpaceAvatarAndNameRow(
                    name = currentSpace.name,
                    avatarData = currentSpace.getAvatarData(AvatarSize.TimelineRoom),
                )
            }
        },
        actions = {
            var showMenu by remember { mutableStateOf(false) }
            IconButton(
                onClick = { showMenu = !showMenu }
            ) {
                Icon(
                    imageVector = CompoundIcons.OverflowVertical(),
                    contentDescription = null,
                )
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    onClick = {
                        showMenu = false
                        onShareSpace()
                    },
                    text = { Text(stringResource(id = CommonStrings.action_share)) },
                    leadingIcon = {
                        Icon(
                            imageVector = CompoundIcons.ShareAndroid(),
                            tint = ElementTheme.colors.iconSecondary,
                            contentDescription = null,
                        )
                    }
                )
                /*
                // TODO re-enable when we have SDK APIs to leave a space
                DropdownMenuItem(
                    onClick = {
                        showMenu = false
                        onLeaveSpaceClick()
                    },
                    text = { Text(stringResource(id = CommonStrings.action_leave)) },
                    leadingIcon = {
                        Icon(
                            imageVector = CompoundIcons.Leave(),
                            tint = ElementTheme.colors.iconSecondary,
                            contentDescription = null,
                        )
                    }
                )
                 */
            }
        },
    )
}

@Composable
private fun SpaceAvatarAndNameRow(
    name: String?,
    avatarData: AvatarData,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(
            avatarData = avatarData,
            avatarType = AvatarType.Space(),
        )
        Text(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .semantics {
                    heading()
                },
            text = name ?: stringResource(CommonStrings.common_no_space_name),
            style = ElementTheme.typography.fontBodyLgMedium,
            fontStyle = FontStyle.Italic.takeIf { name == null },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun SpaceRoom.trailingAction(
    isCurrentlyJoining: Boolean,
    onClick: () -> Unit
): @Composable (() -> Unit)? {
    return when (state) {
        null, CurrentUserMembership.LEFT -> {
            @Composable {
                CompositionLocalProvider(LocalContentColor provides ElementTheme.colors.textActionAccent) {
                    TextButton(
                        text = stringResource(CommonStrings.action_join),
                        onClick = onClick,
                        size = ButtonSize.LargeLowPadding,
                        showProgress = isCurrentlyJoining,
                    )
                }
            }
        }
        else -> null
    }
}

@PreviewsDayNight
@Composable
internal fun SpaceViewPreview(
    @PreviewParameter(SpaceStateProvider::class) state: SpaceState
) = ElementPreview {
    SpaceView(
        state = state,
        onRoomClick = {},
        onShareSpace = {},
        onLeaveSpaceClick = {},
        onBackClick = {},
    )
}
