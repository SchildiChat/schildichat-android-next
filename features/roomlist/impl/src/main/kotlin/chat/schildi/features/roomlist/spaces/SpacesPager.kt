package chat.schildi.features.roomlist.spaces

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBackIos
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import chat.schildi.lib.compose.ScrollableTabRow
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import chat.schildi.lib.compose.TabRowDefaults.tabIndicatorOffset
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
            compactTabs = ScPrefs.COMPACT_ROOT_SPACES.value(),
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
    compactTabs: Boolean,
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
            compactTabs = false,
            content = content,
        )
    } else {
        if (ScPrefs.SPACE_SWIPE.value()) {
            // Swipable content
            var offsetX by remember { mutableFloatStateOf(0f) }
            val draggableState = rememberDraggableState {
                offsetX += it
            }
            // Indicator width itself is 96dp.
            // Indicator threshold: how much we move the indicator out of the screen before swiping
            // Swipe threshold: how much the user should swipe to trigger
            val indicatorThreshold = 104.dp.toPx()
            val swipeThreshold = 74.dp.toPx()
            val decay: DecayAnimationSpec<Float> = rememberSplineBasedDecay()
            // Note: we have spacesList.size+1 tabs
            val canSwipeUp = selectedTab < spacesList.size
            val canSwipeDown = selectedTab > 0
            Box(
                Modifier
                    .weight(1f, fill = true)
                    .draggable(
                        orientation = Orientation.Horizontal,
                        enabled = spacesList.isNotEmpty(),
                        onDragStopped = { velocity ->
                            val targetOffsetX = decay.calculateTargetValue(
                                offsetX,
                                velocity
                            )
                            // Note: we have spacesList.size+1 tabs, index 0 is always default/parent
                            if (targetOffsetX < -swipeThreshold && canSwipeUp) {
                                selectSpaceIndex(selectedTab + 1, spacesList, selectSpace, parentSelection)
                            } else if (targetOffsetX > swipeThreshold && canSwipeDown) {
                                selectSpaceIndex(selectedTab - 1, spacesList, selectSpace, parentSelection)
                            }
                            offsetX = 0f
                        },
                        state = draggableState,
                    )
            ) {
                content(Modifier.fillMaxWidth())
                // Swipe down indicator
                val swipeProgress = min(1f, offsetX.absoluteValue / indicatorThreshold)
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
    val canExpandSelectedTab = !spacesList.getOrNull(selectedSpaceIndex)?.spaces.isNullOrEmpty()
    val renderExpandableIndicatorInTabs = !ScPrefs.COMPACT_ROOT_SPACES.value()
    val tabIndicatorColor = animateColorAsState(
        targetValue = if (expandSpaceChildren || (!canExpandSelectedTab && !renderExpandableIndicatorInTabs))
            MaterialTheme.colorScheme.secondary
        else
            MaterialTheme.colorScheme.primary,
        label = "tabIndicatorColor"
    ).value
    ScrollableTabRow(
        selectedTabIndex = selectedTab,
        edgePadding = 0.dp,
        minTabWidth = 0.dp,
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        indicator = { tabPositions ->
            Box(
                Modifier
                    .tabIndicatorOffset(tabPositions[selectedTab])
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
                    .height(3.dp)
                    .background(color = tabIndicatorColor, shape = RoundedCornerShape(1.5.dp))
            )
        }
    ) {
        if (defaultSpace != null) {
            SpaceTab(defaultSpace, spaceUnreadCounts[defaultSpace.info.roomId.value], selectedTab == 0, expandSpaceChildren, false, compactTabs) {
                expandSpaceChildren = false
                if (selectedTab != 0) {
                    selectSpace(null, parentSelection)
                }
            }
        } else {
            ShowAllTab(spaceUnreadCounts[null], selectedTab == 0, expandSpaceChildren, compactTabs) {
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
                renderExpandableIndicatorInTabs && space.spaces.isNotEmpty(),
                compactTabs,
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
                .alpha(thresholdProgress)
                .background(MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.65f), CircleShape)
                .padding(8.dp)
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
private fun SpaceTabText(text: String, selected: Boolean, expandable: Boolean, collapsed: Boolean) {
    val color = animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
        label = "tabSelectedColor",
    ).value
    Row {
        if (expandable) {
            // We want to keep the text centered despite having an expand-icon
            Spacer(Modifier.width(12.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            color = color,
            modifier = Modifier.widthIn(max = 192.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (expandable) {
            ExpandableIndicator(selected && !collapsed, Modifier.align(Alignment.CenterVertically))
        }
    }
}

@Composable
fun ExpandableIndicator(visible: Boolean, modifier: Modifier = Modifier) {
    Box(modifier.width(12.dp)) {
        androidx.compose.animation.AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun AbstractSpaceTab(
    text: String,
    selected: Boolean,
    collapsed: Boolean,
    expandable: Boolean,
    compact: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
) {
    if (compact) {
        Box(
            Modifier
                .padding(vertical = 8.dp, horizontal = 16.dp)
                .clickable(onClick = onClick)
        ) {
            icon()
            /*
            if (expandable) {
                ExpandableIndicator(
                    selected && !collapsed,
                    Modifier.align(Alignment.CenterEnd).offset(14.dp, 0.dp)
                )
            }
             */
        }
    } else {
        Tab(
            text = { SpaceTabText(text, selected, expandable, collapsed) },
            icon = icon.takeIf { !collapsed },
            selected = selected,
            onClick = onClick,
        )
    }
}

@Composable
private fun SpaceTab(
    space: SpaceListDataSource.SpaceHierarchyItem,
    unreadCounts: SpaceUnreadCountsDataSource.SpaceUnreadCounts?,
    selected: Boolean,
    collapsed: Boolean,
    expandable: Boolean,
    compact: Boolean,
    onClick: () -> Unit
) {
    AbstractSpaceTab(
        text = space.info.name,
        selected = selected,
        collapsed = collapsed,
        expandable = expandable,
        compact = compact,
        onClick = onClick,
    ) {
        UnreadCountBox(unreadCounts, spaceTabUnreadBadgeOffset(compact)) {
            Avatar(space.info.avatarData.copy(size = spaceTabIconSize(compact)), shape = spaceTabIconShape(compact))
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
    compact: Boolean,
    onClick: () -> Unit
) {
    AbstractSpaceTab(
        text = stringResource(id = R.string.screen_roomlist_main_space_title),
        selected = selected,
        collapsed = collapsed,
        expandable = false,
        compact = compact,
        onClick = onClick,
    ) {
        UnreadCountBox(unreadCounts, spaceTabUnreadBadgeOffset(compact)) {
            ShowAllIcon(spaceTabIconSize(compact))
        }
    }
}

@Composable
private fun UnreadCountBox(unreadCounts: SpaceUnreadCountsDataSource.SpaceUnreadCounts?, offset: Dp, content: @Composable () -> Unit) {
    val mode = ScPrefs.SPACE_UNREAD_COUNTS.value()
    if (unreadCounts == null || mode == ScPrefs.SpaceUnreadCountMode.HIDE) {
        content()
        return
    }
    val countChats = mode == ScPrefs.SpaceUnreadCountMode.CHATS
    val count: Int
    val badgeColor: Color
    var outlinedBadge = false
    when {
        unreadCounts.notifiedMessages > 0 -> {
            count = if (countChats) unreadCounts.notifiedChats else unreadCounts.notifiedMessages
            badgeColor = if (unreadCounts.mentionedMessages > 0) ElementTheme.colors.bgCriticalPrimary else ElementTheme.colors.unreadIndicator
        }
        unreadCounts.mentionedMessages > 0 -> {
            count = if (countChats) unreadCounts.mentionedChats else unreadCounts.mentionedMessages
            badgeColor = ElementTheme.colors.bgCriticalPrimary
        }
        unreadCounts.markedUnreadChats > 0 -> {
            count = unreadCounts.markedUnreadChats
            badgeColor = ElementTheme.colors.unreadIndicator
            outlinedBadge = true
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
                .offset(offset, -offset)
                .let {
                    if (outlinedBadge)
                        it
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                            .border(1.dp, badgeColor, RoundedCornerShape(8.dp))
                    else
                        it.background(badgeColor.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                }
                .sizeIn(minWidth = 16.dp, minHeight = 16.dp)
                .align(Alignment.TopEnd)
        ) {
            Text(
                text = formatUnreadCount(count),
                color = if (outlinedBadge) badgeColor else ScTheme.exposures.colorOnAccent,
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

private fun spaceTabIconSize(compact: Boolean) = if (compact) AvatarSize.CompactBottomSpaceBar else AvatarSize.BottomSpaceBar
private fun spaceTabIconShape(compact: Boolean) = if (compact) RoundedCornerShape(8.dp) else RoundedCornerShape(4.dp)
private fun spaceTabUnreadBadgeOffset(compact: Boolean) = if (compact) 6.dp else 8.dp
