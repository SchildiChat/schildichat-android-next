package chat.schildi.features.roomlist.spaces

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBackIos
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import chat.schildi.lib.preferences.ScAppStateStore
import chat.schildi.lib.preferences.ScPrefs
import chat.schildi.lib.preferences.value
import chat.schildi.lib.util.formatUnreadCount
import chat.schildi.theme.ScTheme
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.roomlist.impl.R
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.text.toPx
import io.element.android.libraries.designsystem.theme.unreadIndicator
import io.element.android.libraries.designsystem.utils.OnLifecycleEvent
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun SpacesPager(
    spacesList: ImmutableList<SpaceListDataSource.SpaceHierarchyItem>,
    spaceUnreadCounts: ImmutableMap<String?, SpaceUnreadCountsDataSource.SpaceUnreadCounts>,
    spaceSelectionHierarchy: ImmutableList<String>,
    onSpaceSelected: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (Modifier) -> Unit,
) {
    if (!ScPrefs.SPACE_NAV.value() || spacesList.isEmpty()) {
        content(modifier)
        return
    }
    Column(modifier) {
        SpacesPager(
            spacesList = spacesList,
            spaceUnreadCounts = spaceUnreadCounts,
            spaceSelection = spaceSelectionHierarchy,
            defaultSpace = null,
            parentSelection = persistentListOf(),
            selectSpace = { newSelection, parentSelection ->
                if (newSelection == null) {
                    onSpaceSelected(parentSelection)
                } else {
                    onSpaceSelected(parentSelection + listOf(newSelection.info.roomId.value))
                }
            },
            content = content,
        )
    }
}


@Composable
private fun ColumnScope.SpacesPager(
    spacesList: ImmutableList<SpaceListDataSource.SpaceHierarchyItem>,
    spaceUnreadCounts: ImmutableMap<String?, SpaceUnreadCountsDataSource.SpaceUnreadCounts>,
    spaceSelection: ImmutableList<String>,
    defaultSpace: SpaceListDataSource.SpaceHierarchyItem?,
    parentSelection: ImmutableList<String>,
    selectSpace: (SpaceListDataSource.SpaceHierarchyItem?, ImmutableList<String>) -> Unit,
    content: @Composable (Modifier) -> Unit,
) {
    val selectedSpaceIndex = if (spaceSelection.isEmpty()) {
        -1
    } else {
        spacesList.indexOfFirst { it.info.roomId.value == spaceSelection.first() }
    }
    val childSelections = if (spaceSelection.isEmpty()) spaceSelection else spaceSelection.subList(1, spaceSelection.size)
    if (selectedSpaceIndex < 0 && childSelections.isNotEmpty()) {
        LaunchedEffect(spaceSelection) {
            Timber.w("Invalid space selection detected, clear")
            selectSpace(null, persistentListOf())
        }
        return
    }
    val selectedTab = selectedSpaceIndex + 1

    // Child spaces if expanded
    var expandSpaceChildren by remember { mutableStateOf(childSelections.isNotEmpty()) }

    // Child spaces if expanded
    if (selectedSpaceIndex != -1 && expandSpaceChildren) {
        SpacesPager(
            spacesList = spacesList[selectedSpaceIndex].spaces,
            spaceUnreadCounts = spaceUnreadCounts,
            selectSpace = selectSpace,
            spaceSelection = childSelections,
            defaultSpace = spacesList[selectedSpaceIndex],
            parentSelection = (parentSelection + listOf(spacesList[selectedSpaceIndex].info.roomId.value)).toImmutableList(),
            content = content,
        )
    } else {
        if (ScPrefs.SPACE_SWIPE.value()) {
            // Swipable content
            var offsetX by remember { mutableFloatStateOf(0f) }
            val draggableState = rememberDraggableState {
                offsetX += it
            }
            // Indicator width itself is 96dp
            val swipeThreshold = 104.dp.toPx()
            val indicatorThreshold = 104.dp.toPx()
            // Note: we have spacesList.size+1 tabs
            val canSwipeUp = selectedTab < spacesList.size
            val canSwipeDown = selectedTab > 0
            Box(
                Modifier
                    .weight(1f, fill = true)
                    .draggable(
                        orientation = Orientation.Horizontal,
                        enabled = spacesList.isNotEmpty(),
                        onDragStopped = {
                            // Note: we have spacesList.size+1 tabs, index 0 is always default/parent
                            if (offsetX < -swipeThreshold && canSwipeUp) {
                                selectSpaceIndex(selectedTab + 1, spacesList, selectSpace, parentSelection)
                            } else if (offsetX > swipeThreshold && canSwipeDown) {
                                selectSpaceIndex(selectedTab - 1, spacesList, selectSpace, parentSelection)
                            }
                            offsetX = 0f
                        },
                        state = draggableState,
                    )
            ) {
                content(Modifier.fillMaxWidth())
                // Swipe down indicator
                val swipeProgress = min(1f, offsetX.absoluteValue / swipeThreshold)
                if (canSwipeDown) {
                    SwipeIndicator(
                        if (selectedTab > 1) spacesList[selectedSpaceIndex-1] else defaultSpace, false, swipeProgress,
                        Modifier
                            .align(Alignment.CenterStart)
                            .offset {
                                val x = max(offsetX, 0f) - indicatorThreshold
                                IntOffset(x.roundToInt(), 0)
                            }
                    )
                }
                // Swipe up indicator
                if (canSwipeUp) {
                    SwipeIndicator(
                        spacesList[selectedSpaceIndex+1], true, swipeProgress,
                        Modifier
                            .align(Alignment.CenterEnd)
                            .offset {
                                val x = min(offsetX, 0f) + indicatorThreshold
                                IntOffset(x.roundToInt(), 0)
                            }
                    )
                }
            }
        } else {
            content(Modifier.weight(1f, fill = true))
        }
    }

    // Actual space tabs
    ScrollableTabRow(
        selectedTabIndex = selectedTab,
        edgePadding = 0.dp,
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        if (defaultSpace != null) {
            SpaceTab(defaultSpace, spaceUnreadCounts[defaultSpace.info.roomId.value], selectedTab == 0, expandSpaceChildren, false) {
                expandSpaceChildren = false
                if (selectedTab != 0) {
                    selectSpace(null, parentSelection)
                }
            }
        } else {
            ShowAllTab(spaceUnreadCounts[null], selectedTab == 0, expandSpaceChildren) {
                expandSpaceChildren = false
                if (selectedTab != 0) {
                    selectSpace(null, parentSelection)
                }
            }
        }
        spacesList.forEachIndexed { index, space ->
            val selected = selectedSpaceIndex == index
            SpaceTab(
                space,
                spaceUnreadCounts[space.info.roomId.value],
                selected,
                expandSpaceChildren,
                space.spaces.isNotEmpty() && (!selected || !expandSpaceChildren)
            ) {
                if (selectedSpaceIndex == index) {
                    if (expandSpaceChildren) {
                        expandSpaceChildren = false
                        // In case we selected a child, need to re-select this space
                        if (childSelections.isNotEmpty()) {
                            selectSpace(spacesList[index], parentSelection)
                        }
                    } else if (space.spaces.isNotEmpty()) {
                        expandSpaceChildren = true
                    }
                } else {
                    expandSpaceChildren = false
                    selectSpace(spacesList[index], parentSelection)
                }
            }
        }
    }
}

private fun selectSpaceIndex(
    index: Int,
    spacesList: ImmutableList<SpaceListDataSource.SpaceHierarchyItem>,
    selectSpace: (SpaceListDataSource.SpaceHierarchyItem?, ImmutableList<String>) -> Unit,
    parentSelection: ImmutableList<String>
) {
    if (index == 0) {
        selectSpace(null, parentSelection)
    } else {
        selectSpace(spacesList[index-1], parentSelection)
    }
}

@Composable
private fun SwipeIndicator(space: SpaceListDataSource.SpaceHierarchyItem?, upwards: Boolean, thresholdProgress: Float, modifier: Modifier) {
    Row(modifier) {
        if (upwards) {
            SwipeIndicatorArrow(imageVector = Icons.AutoMirrored.Outlined.ArrowBackIos, thresholdProgress = thresholdProgress)
        }
        Box(
            Modifier
                .background(MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.65f), CircleShape)
                .padding(8.dp)
                .alpha(thresholdProgress)
        ) {
            if (space == null) {
                ShowAllIcon(AvatarSize.SpaceSwipeIndicator, color = MaterialTheme.colorScheme.inverseOnSurface)
            } else {
                Avatar(space.info.avatarData.copy(size = AvatarSize.SpaceSwipeIndicator), shape = CircleShape)
            }
        }
        if (!upwards) {
            SwipeIndicatorArrow(imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos, thresholdProgress = thresholdProgress)
        }
    }
}

@Composable
private fun RowScope.SwipeIndicatorArrow(imageVector: ImageVector, thresholdProgress: Float) {
    Icon(
        imageVector = imageVector,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.65f),
        modifier = Modifier
            .size(48.dp)
            .align(Alignment.CenterVertically)
            .alpha(thresholdProgress)
    )
}

@Composable
private fun AbstractSpaceTab(
    text: String,
    selected: Boolean,
    collapsed: Boolean,
    expandable: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
) {
    Tab(
        text = {
            val color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
            Row {
                if (expandable) {
                    // We want to keep the text centered despite having an expand-icon
                    Spacer(Modifier.width(12.dp))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleSmall,
                    color = color,
                )
                if (expandable) {
                    Box(
                        Modifier
                            .width(12.dp)
                            .align(Alignment.CenterVertically)) {
                        androidx.compose.animation.AnimatedVisibility(visible = selected, enter = fadeIn(), exit = fadeOut()) {
                            Icon(
                                imageVector = Icons.Outlined.ChevronRight,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(12.dp),
                                tint = color,
                            )
                        }
                    }
                }
            }
        },
        icon = icon.takeIf { !collapsed },
        selected = selected,
        onClick = onClick,
    )
}

@Composable
private fun SpaceTab(
    space: SpaceListDataSource.SpaceHierarchyItem,
    unreadCounts: SpaceUnreadCountsDataSource.SpaceUnreadCounts?,
    selected: Boolean,
    collapsed: Boolean,
    expandable: Boolean,
    onClick: () -> Unit
) {
    AbstractSpaceTab(
        text = space.info.name,
        selected = selected,
        collapsed = collapsed,
        expandable = expandable,
        onClick = onClick,
    ) {
        UnreadCountBox(unreadCounts) {
            Avatar(space.info.avatarData.copy(size = AvatarSize.BottomSpaceBar), shape = RoundedCornerShape(4.dp))
        }
    }
}

@Composable
private fun ShowAllIcon(size: AvatarSize, color: Color = MaterialTheme.colorScheme.primary) {
    Icon(
        imageVector = Icons.Filled.Home,
        contentDescription = null,
        modifier = Modifier.size(size.dp),
        tint = color,
    )
}

@Composable
private fun ShowAllTab(
    unreadCounts: SpaceUnreadCountsDataSource.SpaceUnreadCounts?,
    selected: Boolean,
    collapsed: Boolean,
    onClick: () -> Unit
) {
    AbstractSpaceTab(
        text = stringResource(id = R.string.screen_roomlist_main_space_title),
        selected = selected,
        collapsed = collapsed,
        expandable = false,
        onClick = onClick,
    ) {
        UnreadCountBox(unreadCounts) {
            ShowAllIcon(AvatarSize.BottomSpaceBar)
        }
    }
}

@Composable
private fun UnreadCountBox(unreadCounts: SpaceUnreadCountsDataSource.SpaceUnreadCounts?, content: @Composable () -> Unit) {
    val mode = ScPrefs.SPACE_UNREAD_COUNTS.value()
    if (unreadCounts == null || mode == ScPrefs.SpaceUnreadCountMode.HIDE) {
        content()
        return
    }
    val countChats = mode == ScPrefs.SpaceUnreadCountMode.CHATS
    val count: Int
    val badgeColor: Color
    when {
        unreadCounts.notifiedMessages > 0 -> {
            count = if (countChats) unreadCounts.notifiedChats else unreadCounts.notifiedMessages
            badgeColor = if (unreadCounts.mentionedMessages > 0) ElementTheme.colors.bgCriticalPrimary else ElementTheme.colors.unreadIndicator
        }
        unreadCounts.mentionedMessages > 0 -> {
            count = if (countChats) unreadCounts.mentionedChats else unreadCounts.mentionedMessages
            badgeColor = ElementTheme.colors.bgCriticalPrimary
        }
        unreadCounts.unreadMessages > 0 -> {
            count = if (countChats) unreadCounts.unreadChats else unreadCounts.unreadMessages
            badgeColor = ScTheme.exposures.unreadBadgeColor
        }
        else -> {
            // No badge to show
            content()
            return
        }
    }
    Box {
        content()
        Box(
            modifier = Modifier
                .offset(8.dp, (-8).dp)
                .background(badgeColor.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                .sizeIn(minWidth = 16.dp, minHeight = 16.dp)
                .align(Alignment.TopEnd)
        ) {
            Text(
                text = formatUnreadCount(count),
                color = ScTheme.exposures.colorOnAccent,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 2.dp)
            )
        }
        // Keep icon centered
        Spacer(
            Modifier
                .width(8.dp)
                .offset((-8).dp, (-8).dp)
                .align(Alignment.TopStart))
    }
}

@Composable
fun PersistSpaceOnPause(scAppStateStore: ScAppStateStore, spaceAwareRoomListDataSource: SpaceAwareRoomListDataSource) {
    val scope = rememberCoroutineScope()
    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_PAUSE -> spaceAwareRoomListDataSource.spaceSelectionHierarchy.value?.let {
                scope.launch { scAppStateStore.persistSpaceSelection(it) }
            }
            else -> Unit
        }
    }
}
