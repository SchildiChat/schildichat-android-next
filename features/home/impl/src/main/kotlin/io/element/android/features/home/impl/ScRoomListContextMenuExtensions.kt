package io.element.android.features.home.impl

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Workspaces
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import chat.schildi.features.home.spaces.GenericSpaceIcon
import chat.schildi.features.home.spaces.SpaceListDataSource
import chat.schildi.features.home.spaces.flattenWithParents
import chat.schildi.lib.preferences.ScPrefs
import chat.schildi.lib.preferences.value
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.home.impl.roomlist.RoomListContentState
import io.element.android.features.home.impl.roomlist.RoomListState
import io.element.android.libraries.designsystem.components.dialogs.ListDialog
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.theme.components.Checkbox
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.ListItemStyle
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.MatrixClient
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

@Composable
fun LowPriorityRoomListContextMenuItem(
    contextMenu: RoomListState.ContextMenu.Shown,
    onLowPriorityChange: (Boolean) -> Unit,
) {
    if (ScPrefs.BURY_LOW_PRIORITY.value()) {
        ListItem(
            headlineContent = {
                Text(
                    text = stringResource(id = chat.schildi.lib.R.string.sc_action_low_priority),
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            leadingContent = ListItemContent.Icon(
                iconSource = IconSource.Vector(
                    Icons.Default.Archive,
                    contentDescription = stringResource(id = chat.schildi.lib.R.string.sc_action_low_priority),
                )
            ),
            trailingContent = ListItemContent.Switch(
                checked = contextMenu.isLowPriority,
            ),
            onClick = {
                onLowPriorityChange(!contextMenu.isLowPriority)
            },
            style = ListItemStyle.Primary,
        )
    }
}

data class SpaceSelectionEntry(
    val space: SpaceListDataSource.SpaceHierarchyItem,
    val parents: ImmutableList<SpaceListDataSource.AbstractSpaceHierarchyItem>,
    val isDirectParent: Boolean,
) {
    override fun toString(): String {
        return "SpaceSelector(${space.info.roomId}, isDirectParent=$isDirectParent, permission=$userHasPermission)"
    }
    val userHasPermission: Boolean
        get() = space.info.canUserManageSpaces
}

sealed interface PendingSpaceManagementAction {
    val selection: SpaceSelectionEntry
    val space: SpaceListDataSource.SpaceHierarchyItem
        get() = selection.space

    data class Add(override val selection: SpaceSelectionEntry): PendingSpaceManagementAction
    data class Remove(override val selection: SpaceSelectionEntry): PendingSpaceManagementAction
}

@Composable
fun ManageSpacesRoomListContextMenuItems(
    contextMenu: RoomListState.ContextMenu.Shown,
    roomListState: RoomListState,
    // Only nullable as hack to not break previews and be lazy with building new node/overhead demanded by upstream architecture
    matrixClient: MatrixClient? = null,
) {
    if (ScPrefs.SPACE_NAV.value()) {
        var showManagementDialog by remember { mutableStateOf(false) }
        if (showManagementDialog) {
            ManageParentSpacesDialog(
                contextMenu = contextMenu,
                roomListState = roomListState,
                matrixClient = matrixClient,
                dismiss = { showManagementDialog = false }
            )
        }
        ListItem(
            headlineContent = {
                Text(
                    text = stringResource(chat.schildi.lib.R.string.sc_action_space_management),
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            modifier = Modifier.clickable {
                showManagementDialog = true
            },
            leadingContent = ListItemContent.Icon(
                iconSource = IconSource.Vector(
                    Icons.Default.Workspaces,
                    contentDescription = stringResource(chat.schildi.lib.R.string.sc_action_space_management),
                )
            ),
            style = ListItemStyle.Primary,
        )
    }
}

@Composable
fun ManageParentSpacesDialog(
    contextMenu: RoomListState.ContextMenu.Shown,
    roomListState: RoomListState,
    // Only nullable as hack to not break previews and be lazy with building new node/overhead demanded by upstream architecture
    matrixClient: MatrixClient? = null,
    dismiss: () -> Unit,
) {
    val context = LocalContext.current
    val roomId = contextMenu.roomId

    // To not re-sort while user is viewing at it, remember original checked state...
    val originallyCheckedState = remember { mutableStateMapOf<String, Boolean>() }
    // Lazy local echos
    val pendingState = remember { mutableStateMapOf<String, Boolean>() }
    val expectedState = remember { mutableStateMapOf<String, Boolean>() }
    // Pending actions
    val pendingSpaceActions = remember { mutableStateListOf<PendingSpaceManagementAction>() }
    val pendingActionsInProgress by remember { mutableStateOf(false) }

    var spaceSelection by remember { mutableStateOf<ImmutableList<SpaceSelectionEntry>?>(null) }
    val spacesList = (roomListState.contentState as? RoomListContentState.Rooms)?.spacesList

    LaunchedEffect(roomId, spacesList) {
        spaceSelection = spacesList?.flattenWithParents()?.mapNotNull { (space, parents) ->
            if (space is SpaceListDataSource.SpaceHierarchyItem) {
                // Note: would be nice to check if user has permission to enrich UI. We can get that information via
                //  matrixClient?.getRoom(space.info.roomId)?.canManageSpaceChildren().getOrNull() == true
                //  but `getRoom()` is too expensive to do here, as it builds a fully blown timeline.
                SpaceSelectionEntry(
                    space = space,
                    parents = parents.sortedBy { it.name.lowercase() }.toImmutableList(),
                    isDirectParent = space.directChildren.contains(roomId.value),
                )
            } else {
                null
            }
        }
            ?.sortedWith(compareBy({ !it.userHasPermission }, { !(originallyCheckedState[it.space.selectionId] ?: it.isDirectParent) }, { it.space.name.lowercase() }))
            ?.toImmutableList()
    }
    val indexOfFirstPermissionDeniedSpace = remember(spaceSelection) { spaceSelection?.indexOfFirst { !it.userHasPermission }.takeIf { it != -1 } }

    fun handleItemSelect(item: SpaceSelectionEntry, select: Boolean) {
        // Sanity check for permissions
        if (!item.userHasPermission) {
            Timber.tag("SpaceMan").d("Clicked space without permission")
            return
        }
        val selectionId = item.space.selectionId
        val wasSelected = pendingState[selectionId] ?: expectedState[selectionId] ?: item.isDirectParent
        // Sanity check if we handled it before
        if (wasSelected == select) {
            Timber.tag("SpaceMan").d("Clicked space without changing selection state")
            return
        }
        // Persist original toggle state for sort order if necessary
        if (!originallyCheckedState.contains(selectionId)) {
            originallyCheckedState[selectionId] = item.isDirectParent
        }
        // Remove previous actions
        pendingSpaceActions.removeIf { it.space.selectionId == selectionId }
        // Add new action if necessary
        if (select != item.isDirectParent) {
            val pendingSpaceAction = if (select) {
                PendingSpaceManagementAction.Add(item)
            } else {
                PendingSpaceManagementAction.Remove(item)
            }
            pendingSpaceActions.add(pendingSpaceAction)
        }
        // Update UI with new selection state
        pendingState[selectionId] = select
    }
    val scope = rememberCoroutineScope()
    ListDialog(
        onSubmit = {
            scope.launch(Dispatchers.IO) {
                Timber.tag("SpaceMan").d("Start processing ${pendingSpaceActions.size} actions")
                while (pendingSpaceActions.isNotEmpty()) {
                    val action = pendingSpaceActions.removeAt(0)
                    val room = matrixClient?.getRoom(action.space.info.roomId)
                    if (room == null) {
                        Timber.tag("SpaceMan").e("Failed to find room for space ${action.space.info.roomId}")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Space ${action.space.info.name ?: action.space.info.roomId} not found", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Timber.tag("SpaceMan").d("Starting action $action")
                        val selectionId = action.space.selectionId
                        val result = when (action) {
                            is PendingSpaceManagementAction.Add -> {
                                expectedState[selectionId] = true
                                room.addSpaceChild(roomId)
                            }
                            is PendingSpaceManagementAction.Remove -> {
                                expectedState[selectionId] = false
                                room.removeSpaceChild(roomId)
                            }
                        }
                        if (result.isSuccess) {
                            Timber.tag("SpaceMan").d("Finished action $action")
                        } else {
                            Timber.tag("SpaceMan").e("Failed to execute space management action: ${result.exceptionOrNull()}")
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, result.exceptionOrNull()?.toString() ?: "Unknown error", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }
        },
        onDismissRequest = dismiss,
        title = stringResource(chat.schildi.lib.R.string.sc_action_assign_room_to_spaces, contextMenu.roomName ?: contextMenu.roomId.value),
        enabled = pendingSpaceActions.isNotEmpty(),
        submitText = stringResource(chat.schildi.lib.R.string.sc_action_apply),
        cancelText = if (pendingSpaceActions.isEmpty())
            stringResource(io.element.android.libraries.ui.strings.R.string.action_done)
        else
            stringResource(io.element.android.libraries.ui.strings.R.string.action_cancel)
    ) {
        val spaceItems = spaceSelection
        when {
            spaceItems == null -> {
                item {
                    Box(Modifier.fillMaxWidth()) {
                        CircularProgressIndicator(
                            Modifier.size(48.dp).padding(horizontal = 8.dp).align(Alignment.Center)
                        )
                    }
                }
            }
            spaceItems.isEmpty() -> {
                item {
                    Text(
                        stringResource(chat.schildi.lib.R.string.sc_space_list_empty),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            else -> {
                itemsIndexed(spaceItems) { index, item ->
                    if (index == indexOfFirstPermissionDeniedSpace) {
                        PermissionDeniedTitle()
                    }
                    val isCheckedPerLocalEcho = pendingState[item.space.selectionId]
                    val isServerEchoPending = expectedState[item.space.selectionId]?.let { it == item.isDirectParent } == false
                    val clickable = item.userHasPermission && !isServerEchoPending && !pendingActionsInProgress
                    Row(Modifier.fillMaxWidth().clickable(enabled = clickable) {
                        handleItemSelect(item, !(isCheckedPerLocalEcho ?: item.isDirectParent))
                    }) {
                        val spaceHasPendingActionInProgress = isServerEchoPending || pendingActionsInProgress && pendingSpaceActions.any {
                            it.space.selectionId == item.space.selectionId
                        }
                        Box(Modifier.size(48.dp).align(Alignment.CenterVertically).alpha(if (clickable) 1f else ElementTheme.colors.textDisabled.alpha)) {
                            AnimatedContent(
                                spaceHasPendingActionInProgress,
                                modifier = Modifier.size(48.dp),
                                label = "SpaceSelectionState"
                            ) { showSpinner ->
                                if (showSpinner) {
                                    Box(
                                        modifier = Modifier.size(24.dp).align(Alignment.Center).graphicsLayer {
                                            // Workaround to force the spinner smaller than it wants to
                                            scaleX = 0.5f
                                            scaleY = 0.5f
                                        },
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(48.dp).align(Alignment.Center),
                                            strokeWidth = 4.dp
                                        )
                                    }
                                } else {
                                    Checkbox(
                                        checked = isCheckedPerLocalEcho ?: item.isDirectParent,
                                        onCheckedChange = { checked ->
                                            if (!clickable) return@Checkbox
                                            handleItemSelect(item, checked)
                                        },
                                        enabled = clickable,
                                        modifier = Modifier.size(48.dp),
                                    )
                                }
                            }
                        }
                        GenericSpaceIcon(
                            space = item.space,
                            modifier = Modifier.align(Alignment.CenterVertically),
                        )
                        Column(Modifier.align(Alignment.CenterVertically).padding(start = 8.dp, top = 2.dp, bottom = 2.dp)) {
                            Text(
                                item.space.name,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = ElementTheme.colors.textPrimary.withDisabledAlpha(clickable),
                            )
                            if (item.parents.isNotEmpty()) {
                                Text(
                                    stringResource(chat.schildi.lib.R.string.sc_space_is_sub_space_of, item.parents.joinToString { it.name }),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = ElementTheme.colors.textSecondary.withDisabledAlpha(clickable),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Color.withDisabledAlpha(enabled: Boolean) = if (enabled) this else copy(alpha = alpha * ElementTheme.colors.textDisabled.alpha)

@Composable
private fun PermissionDeniedTitle() {
    Text(
        text = stringResource(chat.schildi.lib.R.string.sc_space_list_permissions_missing_for_following),
        color = ElementTheme.colors.textPrimary,
        style = ElementTheme.typography.fontBodyLgMedium,
        textAlign = TextAlign.Left,
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp, start = 12.dp, end = 12.dp),
    )
}
