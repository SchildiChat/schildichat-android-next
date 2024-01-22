package chat.schildi.features.roomlist.spaces

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
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
import io.element.android.libraries.designsystem.theme.unreadIndicator
import io.element.android.libraries.designsystem.utils.OnLifecycleEvent
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import timber.log.Timber

private val SpaceAvatarSize = AvatarSize.BottomSpaceBar

@Composable
fun SpacesPager(
    spacesList: ImmutableList<SpaceListDataSource.SpaceHierarchyItem>,
    spaceUnreadCounts: ImmutableMap<String?, SpaceUnreadCountsDataSource.SpaceUnreadCounts>,
    spaceSelectionHierarchy: ImmutableList<String>,
    onSpaceSelected: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (Modifier) -> Unit,
) {
    if (!ScPrefs.SPACE_NAV.value()) {
        content(modifier)
        return
    }
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
        modifier = modifier,
        content = content,
    )
}


@Composable
private fun SpacesPager(
    spacesList: ImmutableList<SpaceListDataSource.SpaceHierarchyItem>,
    spaceUnreadCounts: ImmutableMap<String?, SpaceUnreadCountsDataSource.SpaceUnreadCounts>,
    spaceSelection: ImmutableList<String>,
    defaultSpace: SpaceListDataSource.SpaceHierarchyItem?,
    parentSelection: ImmutableList<String>,
    selectSpace: (SpaceListDataSource.SpaceHierarchyItem?, ImmutableList<String>) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (Modifier) -> Unit,
) {
    if (spacesList.isEmpty()) {
        content(modifier)
        return
    }
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
    Column(modifier = modifier) {
        content(Modifier.weight(1f, fill = true))

        // Child spaces if expanded
        var expandSpaceChildren by remember { mutableStateOf(childSelections.isNotEmpty()) }
        if (selectedSpaceIndex != -1 && expandSpaceChildren) {
            SpacesPager(
                spacesList = spacesList[selectedSpaceIndex].spaces,
                spaceUnreadCounts = spaceUnreadCounts,
                selectSpace = selectSpace,
                spaceSelection = childSelections,
                defaultSpace = spacesList[selectedSpaceIndex],
                parentSelection = (parentSelection + listOf(spacesList[selectedSpaceIndex].info.roomId.value)).toImmutableList(),
            ) {}
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
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleSmall,
                    color = color,
                )
                if (expandable && selected) {
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier
                            .size(12.dp)
                            .align(Alignment.CenterVertically),
                        tint = color,
                    )
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
            Avatar(space.info.avatarData.copy(size = SpaceAvatarSize), shape = RoundedCornerShape(4.dp))
        }
    }
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
            Icon(
                imageVector = Icons.Filled.Home,
                contentDescription = null,
                modifier = Modifier.size(SpaceAvatarSize.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
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
