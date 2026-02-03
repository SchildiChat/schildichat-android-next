package chat.schildi.features.home.spaces

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
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import chat.schildi.lib.compose.TabRowDefaults
import chat.schildi.lib.compose.TabRowDefaults.tabIndicatorOffset
import chat.schildi.lib.preferences.ScAppStateStore
import chat.schildi.lib.preferences.ScPrefs
import chat.schildi.lib.preferences.value
import chat.schildi.lib.util.formatUnreadCount
import chat.schildi.theme.ScTheme
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.home.impl.HomeState
import io.element.android.features.home.impl.spaces.HomeSpacesView
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.text.toPx
import io.element.android.libraries.designsystem.theme.unreadIndicator
import io.element.android.libraries.designsystem.utils.OnLifecycleEvent
import io.element.android.libraries.matrix.api.core.RoomId
import kotlinx.collections.immutable.ImmutableList
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
    homeState: HomeState?,
    lazyListState: LazyListState,
    spacesList: ImmutableList<SpaceListDataSource.AbstractSpaceHierarchyItem>,
    totalUnreadCounts: SpaceUnreadCountsDataSource.SpaceUnreadCounts?,
    spaceSelectionHierarchy: ImmutableList<String>,
    onSpaceSelected: (List<String>) -> Unit,
    onUpstreamSpaceClick: (RoomId) -> Unit,
    onCreateSpaceClick: () -> Unit,
    onExploreClick: () -> Unit,
    onMeasureSpaceBarHeight: (Int) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (Modifier) -> Unit,
) {
    if (!ScPrefs.SPACE_NAV.value() || spacesList.isEmpty()) {
        content(modifier)
        LaunchedEffect(Unit) { onMeasureSpaceBarHeight(0) }
        return
    }
    Column(modifier) {
        SpacesPager(
            homeState = homeState,
            lazyListState = lazyListState,
            spacesList = spacesList,
            totalUnreadCounts = totalUnreadCounts,
            spaceSelection = spaceSelectionHierarchy,
            defaultSpace = null,
            parentSelection = persistentListOf(),
            selectSpace = { newSelection, parentSelection ->
                if (newSelection == null) {
                    onSpaceSelected(parentSelection)
                } else {
                    onSpaceSelected(parentSelection + listOf(newSelection.selectionId))
                }
            },
            compactTabs = ScPrefs.COMPACT_ROOT_SPACES.value(),
            onUpstreamSpaceClick = onUpstreamSpaceClick,
            onCreateSpaceClick = onCreateSpaceClick,
            onExploreClick = onExploreClick,
            onMeasureSpaceBarHeight = onMeasureSpaceBarHeight,
            content = content,
        )
    }
}


@Composable
private fun ColumnScope.SpacesPager(
    homeState: HomeState?,
    lazyListState: LazyListState,
    spacesList: ImmutableList<SpaceListDataSource.AbstractSpaceHierarchyItem>,
    totalUnreadCounts: SpaceUnreadCountsDataSource.SpaceUnreadCounts?,
    spaceSelection: ImmutableList<String>,
    defaultSpace: SpaceListDataSource.AbstractSpaceHierarchyItem?,
    parentSelection: ImmutableList<String>,
    selectSpace: (SpaceListDataSource.AbstractSpaceHierarchyItem?, ImmutableList<String>) -> Unit,
    compactTabs: Boolean,
    onUpstreamSpaceClick: (RoomId) -> Unit,
    onCreateSpaceClick: () -> Unit,
    onExploreClick: () -> Unit,
    onMeasureSpaceBarHeight: (Int) -> Unit,
    content: @Composable (Modifier) -> Unit,
) {
    val selectedSpaceIndex = if (spaceSelection.isEmpty()) {
        -1
    } else {
        spacesList.indexOfFirst { it.selectionId == spaceSelection.first() }
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

    val thisSpaceBarHeight = remember { mutableIntStateOf(0) }
    val childrenSpaceBarHeight = remember { mutableIntStateOf(0) }
    fun onSpaceBarHeightUpdate() {
        if (expandSpaceChildren) {
            onMeasureSpaceBarHeight(thisSpaceBarHeight.intValue + childrenSpaceBarHeight.intValue)
        } else {
            onMeasureSpaceBarHeight(thisSpaceBarHeight.intValue)
        }
    }

    val allowAllRooms = defaultSpace != null || ScPrefs.PSEUDO_SPACE_ALL_ROOMS.value()

    // Child spaces if expanded
    if (selectedSpaceIndex != -1 && expandSpaceChildren) {
        val safeSpace = spacesList[selectedSpaceIndex] as? SpaceListDataSource.SpaceHierarchyItem
        if (safeSpace != null) {
            SpacesPager(
                homeState = homeState,
                lazyListState = lazyListState,
                spacesList = safeSpace.spaces,
                totalUnreadCounts = totalUnreadCounts,
                selectSpace = selectSpace,
                spaceSelection = childSelections,
                defaultSpace = spacesList[selectedSpaceIndex],
                parentSelection = (parentSelection + listOf(spacesList[selectedSpaceIndex].selectionId)).toImmutableList(),
                compactTabs = false,
                onUpstreamSpaceClick = onUpstreamSpaceClick,
                onCreateSpaceClick = onCreateSpaceClick,
                onExploreClick = onExploreClick,
                onMeasureSpaceBarHeight = {
                    childrenSpaceBarHeight.intValue = it
                    onSpaceBarHeightUpdate()
                },
                content = content,
            )
        }
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
            val canSwipeDown = if (allowAllRooms) selectedTab > 0 else selectedTab > 1
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
                if (homeState != null && spaceSelection.isNotEmpty() && spacesList[selectedSpaceIndex] is SpaceListDataSource.UpstreamSpaceListItem) {
                    HomeSpacesView(
                        modifier = Modifier
                            .fillMaxWidth(),
                        state = homeState.homeSpacesState,
                        lazyListState = lazyListState,
                        onSpaceClick = onUpstreamSpaceClick,
                        onCreateSpaceClick = onCreateSpaceClick,
                        onExploreClick = onExploreClick,
                    )
                } else {
                    content(Modifier.fillMaxWidth())
                }
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
            Box(Modifier.weight(1f, fill = true)) {
                content(Modifier.fillMaxWidth())
            }
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
    val selectedTabRendered = selectedTab.correctDownIfNot(allowAllRooms)
    ScrollableTabRow(
        selectedTabIndex = selectedTabRendered,
        edgePadding = 0.dp,
        minTabWidth = 0.dp,
        containerColor = ScTheme.exposures.spaceBarBg ?: TabRowDefaults.primaryContainerColor,
        indicator = { tabPositions ->
            Box(
                Modifier
                    .tabIndicatorOffset(tabPositions.getOrNull(selectedTabRendered) ?: tabPositions[0])
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
                    .height(3.dp)
                    .background(color = tabIndicatorColor, shape = RoundedCornerShape(1.5.dp))
            )
        },
        modifier = Modifier.onGloballyPositioned {
            thisSpaceBarHeight.intValue = it.size.height
            onSpaceBarHeightUpdate()
        },
    ) {
        if (allowAllRooms) {
            if (defaultSpace != null) {
                SpaceTab(defaultSpace, selectedTab == 0, expandSpaceChildren, false, compactTabs) {
                    expandSpaceChildren = false
                    if (selectedTab != 0) {
                        selectSpace(null, parentSelection)
                    }
                }
            } else {
                ShowAllTab(totalUnreadCounts, selectedTab == 0, expandSpaceChildren, compactTabs) {
                    expandSpaceChildren = false
                    if (selectedTab != 0) {
                        selectSpace(null, parentSelection)
                    }
                }
            }
        }
        spacesList.forEachIndexed { index, space ->
            val selected = selectedSpaceIndex == index
            SpaceTab(
                space,
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

private fun Int.correctDownIfNot(condition: Boolean) = if (condition) this else dec()
private fun Int.correctUpIfNot(condition: Boolean) = if (condition) this else inc()

private fun selectSpaceIndex(
    index: Int,
    spacesList: ImmutableList<SpaceListDataSource.AbstractSpaceHierarchyItem>,
    selectSpace: (SpaceListDataSource.AbstractSpaceHierarchyItem?, ImmutableList<String>) -> Unit,
    parentSelection: ImmutableList<String>
) {
    if (index == 0) {
        selectSpace(null, parentSelection)
    } else {
        selectSpace(spacesList[index-1], parentSelection)
    }
}

@Composable
private fun SwipeIndicator(space: SpaceListDataSource.AbstractSpaceHierarchyItem?, upwards: Boolean, thresholdProgress: Float, modifier: Modifier) {
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
            AbstractSpaceIcon(space, AvatarSize.SpaceSwipeIndicator, color = MaterialTheme.colorScheme.inverseOnSurface)
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
                .clickable(onClick = onClick)
                .padding(vertical = 8.dp, horizontal = 16.dp)
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
    space: SpaceListDataSource.AbstractSpaceHierarchyItem,
    selected: Boolean,
    collapsed: Boolean,
    expandable: Boolean,
    compact: Boolean,
    onClick: () -> Unit
) {
    AbstractSpaceTab(
        text = space.name,
        selected = selected,
        collapsed = collapsed,
        expandable = expandable,
        compact = compact,
        onClick = onClick,
    ) {
        UnreadCountBox(space.unreadCounts, spaceTabUnreadBadgeOffset(compact)) {
            AbstractSpaceIcon(space = space, size = spaceTabIconSize(compact), shape = spaceTabIconShape(compact))
        }
    }
}

@Composable
private fun AbstractSpaceIcon(
    space: SpaceListDataSource.AbstractSpaceHierarchyItem?,
    size: AvatarSize,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    shape: Shape = CircleShape,
) {
    when(space) {
        is SpaceListDataSource.SpaceHierarchyItem -> Avatar(space.info.avatarData.copy(size = size), avatarType = AvatarType.Sc(shape), modifier = modifier)
        is SpaceListDataSource.PseudoSpaceItem -> PseudoSpaceIcon(imageVector = space.icon, size = size, color = color, modifier = modifier)
        else -> PseudoSpaceIcon(Icons.Filled.Home, AvatarSize.SpaceSwipeIndicator, color = color, modifier = modifier)
    }
}

@Composable
private fun PseudoSpaceIcon(
    imageVector: ImageVector,
    size: AvatarSize,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Icon(
        imageVector = imageVector,
        contentDescription = null,
        modifier = modifier.size(size.dp),
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
        text = stringResource(id = chat.schildi.lib.R.string.sc_space_all_rooms_title),
        selected = selected,
        collapsed = collapsed,
        expandable = false,
        compact = compact,
        onClick = onClick,
    ) {
        UnreadCountBox(unreadCounts, spaceTabUnreadBadgeOffset(compact)) {
            PseudoSpaceIcon(Icons.Filled.Home, spaceTabIconSize(compact))
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
    val count: Long
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
        unreadCounts.unreadMessages > 0 && ScPrefs.RENDER_SILENT_UNREAD.value() -> {
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
                .width(offset)
                .offset(-offset, -offset)
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

// For other places in the UI wanting to render space icons
@Composable
fun GenericSpaceIcon(
    space: SpaceListDataSource.AbstractSpaceHierarchyItem?,
    modifier: Modifier = Modifier,
    size: AvatarSize = spaceTabIconSize(false),
    shape: Shape = spaceTabIconShape(true),
) {
    AbstractSpaceIcon(space = space, size = size, shape = shape, modifier = modifier)
}
