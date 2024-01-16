package chat.schildi.features.roomlist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import chat.schildi.lib.preferences.ScPrefs
import chat.schildi.lib.preferences.value
import io.element.android.features.roomlist.impl.R
import io.element.android.features.roomlist.impl.datasource.SpaceListDataSource
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import timber.log.Timber

private val SpaceAvatarSize = AvatarSize.BottomSpaceBar

@Composable
fun SpacesPager(
    spacesList: ImmutableList<SpaceListDataSource.SpaceHierarchyItem>,
    onSpaceSelected: (List<String>) -> Unit,
    spaceSelectionHierarchy: ImmutableList<String>,
    modifier: Modifier = Modifier,
    content: @Composable (Modifier) -> Unit,
) {
    if (!ScPrefs.SPACE_NAV.value()) {
        content(modifier)
        return
    }
    SpacesPager(
        spacesList = spacesList,
        selectSpace = { newSelection, parentSelection ->
            if (newSelection == null) {
                onSpaceSelected(parentSelection)
            } else {
                onSpaceSelected(parentSelection + listOf(newSelection.info.roomId.value))
            }
        },
        modifier = modifier,
        spaceSelection = spaceSelectionHierarchy,
        defaultSpace = null,
        parentSelection = persistentListOf(),
        content = content,
    )
}


@Composable
private fun SpacesPager(
    spacesList: ImmutableList<SpaceListDataSource.SpaceHierarchyItem>,
    selectSpace: (SpaceListDataSource.SpaceHierarchyItem?, ImmutableList<String>) -> Unit,
    modifier: Modifier = Modifier,
    spaceSelection: ImmutableList<String>,
    defaultSpace: SpaceListDataSource.SpaceHierarchyItem?,
    parentSelection: ImmutableList<String>,
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
                SpaceTab(defaultSpace, selectedTab == 0, expandSpaceChildren, false) {
                    expandSpaceChildren = false
                    selectSpace(defaultSpace, parentSelection)
                }
            } else {
                ShowAllTab(selected = selectedTab == 0, expandSpaceChildren) {
                    expandSpaceChildren = false
                    selectSpace(null, parentSelection)
                }
            }
            spacesList.forEachIndexed { index, space ->
                val selected = selectedSpaceIndex == index
                SpaceTab(space, selected, expandSpaceChildren, space.spaces.isNotEmpty() && (!selected || !expandSpaceChildren)) {
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
    icon: @Composable () -> Unit
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
                        modifier = Modifier.size(12.dp).align(Alignment.CenterVertically),
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
    selected: Boolean,
    collapsed: Boolean,
    expandable: Boolean,
    onClick: () -> Unit
) {
    AbstractSpaceTab(text = space.info.name, selected = selected, collapsed = collapsed, expandable = expandable, onClick = onClick) {
        Avatar(space.info.avatarData.copy(size = SpaceAvatarSize), shape = RoundedCornerShape(4.dp))
    }
}

@Composable
private fun ShowAllTab(selected: Boolean, collapsed: Boolean, onClick: () -> Unit) {
    AbstractSpaceTab(text = stringResource(id = R.string.screen_roomlist_main_space_title), selected = selected, collapsed = collapsed, expandable = false, onClick = onClick) {
        Icon(
            imageVector = Icons.Filled.Home,
            contentDescription = null,
            modifier = Modifier.size(SpaceAvatarSize.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}
