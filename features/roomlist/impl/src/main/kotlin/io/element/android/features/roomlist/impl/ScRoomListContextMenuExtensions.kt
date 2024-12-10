package io.element.android.features.roomlist.impl

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Workspaces
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import chat.schildi.features.roomlist.spaces.AbstractSpaceIcon
import chat.schildi.features.roomlist.spaces.SpaceListDataSource
import chat.schildi.features.roomlist.spaces.flattenWithParents
import chat.schildi.lib.preferences.ScPrefs
import chat.schildi.lib.preferences.value
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.theme.components.Checkbox
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.ListItemStyle
import io.element.android.libraries.designsystem.theme.components.Surface
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.MatrixClient
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import timber.log.Timber

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

sealed interface PendingSpaceManagementState {
    val selection: SpaceSelectionEntry
    val parentSpace: SpaceListDataSource.SpaceHierarchyItem
        get() = selection.space

    sealed interface InProgress : PendingSpaceManagementState
    sealed interface Result : PendingSpaceManagementState

    data class Add(override val selection: SpaceSelectionEntry): InProgress
    data class Remove(override val selection: SpaceSelectionEntry): InProgress
    data class Error(override val selection: SpaceSelectionEntry, val msg: String): Result
    data class Success(override val selection: SpaceSelectionEntry): Result
}

@Composable
fun ManageSpacesRoomListContextMenuItems(
    contextMenu: RoomListState.ContextMenu.Shown,
    roomListState: RoomListState,
    // Only nullable as hack to not break previews and be lazy with building new node/overhead demanded by upstream architecture
    matrixClient: MatrixClient? = null,
) {
    if (ScPrefs.SPACE_MANAGEMENT.value()) {
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

@OptIn(ExperimentalMaterial3Api::class)
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
    val expectedState = remember { mutableStateMapOf<String, Boolean>() }

    var spaceSelection by remember { mutableStateOf<ImmutableList<SpaceSelectionEntry>?>(null) }
    val spacesList = (roomListState.contentState as? RoomListContentState.Rooms)?.spacesList
    var pendingSpaceAction by remember { mutableStateOf<PendingSpaceManagementState?>(null) }

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
    pendingSpaceAction?.let { pendingSpaceActionValue ->
        LaunchedEffect(pendingSpaceActionValue) {
            if (pendingSpaceActionValue is PendingSpaceManagementState.InProgress) {
                val room = matrixClient?.getRoom(pendingSpaceActionValue.parentSpace.info.roomId)
                pendingSpaceAction = if (room == null) {
                    Timber.e("Failed to find room for space ${pendingSpaceActionValue.parentSpace.info.roomId}")
                    PendingSpaceManagementState.Error(pendingSpaceActionValue.selection, "Not found")
                } else {
                    val selectionId = pendingSpaceActionValue.parentSpace.selectionId
                    if (selectionId !in originallyCheckedState) {
                        originallyCheckedState[selectionId] = pendingSpaceActionValue.selection.isDirectParent
                    }
                    val result = when (pendingSpaceActionValue) {
                        is PendingSpaceManagementState.Add -> {
                            expectedState[selectionId] = true
                            room.addSpaceChild(roomId)
                        }
                        is PendingSpaceManagementState.Remove -> {
                            expectedState[selectionId] = false
                            room.removeSpaceChild(roomId)
                        }
                    }
                    if (result.isSuccess) {
                        PendingSpaceManagementState.Success(pendingSpaceActionValue.selection)
                    } else {
                        Timber.e("Failed to execute space management action: ${result.exceptionOrNull()}")
                        PendingSpaceManagementState.Error(pendingSpaceActionValue.selection, result.exceptionOrNull().toString())
                    }
                }
            } else if (pendingSpaceActionValue is PendingSpaceManagementState.Result) {
                if (pendingSpaceActionValue is PendingSpaceManagementState.Error) {
                    Toast.makeText(context, pendingSpaceActionValue.msg, Toast.LENGTH_LONG).show()
                    expectedState.remove(pendingSpaceActionValue.parentSpace.selectionId)
                }
                delay(3000)
                pendingSpaceAction = null
            }
        }
    }
    BasicAlertDialog(onDismissRequest = dismiss) {
        Surface(Modifier.background(ElementTheme.materialColors.surfaceBright, RoundedCornerShape(12.dp))) {
            val spaceItems = spaceSelection
            if (spaceItems == null) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Spacer(Modifier.height(8.dp))
                    SelectSpaceTitle(contextMenu)
                    CircularProgressIndicator(
                        modifier = Modifier.size(AvatarSize.BottomSpaceBar.dp).padding(horizontal = 8.dp).align(Alignment.CenterHorizontally)
                    )
                }
            } else {
                val lazyListState = rememberLazyListState()
                LazyColumn(
                    state = lazyListState,
                    contentPadding = PaddingValues(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    item {
                        SelectSpaceTitle(contextMenu)
                        Spacer(Modifier.height(2.dp))
                    }
                    if (spaceItems.isEmpty()) {
                        item {
                            Text(
                                stringResource(chat.schildi.lib.R.string.sc_space_list_empty),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                    itemsIndexed(spaceItems) { index, item ->
                        if (index == indexOfFirstPermissionDeniedSpace) {
                            PermissionDeniedTitle()
                        }
                        val isCheckedPerLocalEcho = expectedState[item.space.selectionId]
                        val isLocalEchoPending = isCheckedPerLocalEcho?.let { it == item.isDirectParent } == false
                        val clickable = item.userHasPermission && !isLocalEchoPending
                        Row(Modifier.fillMaxWidth().clickable(enabled = clickable) {
                            pendingSpaceAction = if (isCheckedPerLocalEcho ?: item.isDirectParent) {
                                PendingSpaceManagementState.Remove(item)
                            } else {
                                PendingSpaceManagementState.Add(item)
                            }
                        }) {
                            val spaceHasPendingActionInProgress = isLocalEchoPending ||
                                (pendingSpaceAction as? PendingSpaceManagementState.InProgress)?.parentSpace?.selectionId == item.space.selectionId
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
                                                pendingSpaceAction = if (checked) {
                                                    PendingSpaceManagementState.Add(item)
                                                } else {
                                                    PendingSpaceManagementState.Remove(item)
                                                }
                                            },
                                            enabled = clickable,
                                            modifier = Modifier.size(48.dp),
                                        )
                                    }
                                }
                            }
                            AbstractSpaceIcon(
                                space = item.space,
                                size = AvatarSize.BottomSpaceBar,
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
}

@Composable
private fun Color.withDisabledAlpha(enabled: Boolean) = if (enabled) this else copy(alpha = alpha * ElementTheme.colors.textDisabled.alpha)

@Composable
private fun SelectSpaceTitle(contextMenu: RoomListState.ContextMenu.Shown) {
    Text(
        text = stringResource(chat.schildi.lib.R.string.sc_action_assign_room_to_spaces, contextMenu.roomName ?: contextMenu.roomId.value),
        color = ElementTheme.colors.textPrimary,
        style = ElementTheme.typography.fontHeadingSmRegular,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(8.dp),
    )
}


@Composable
private fun PermissionDeniedTitle() {
    Text(
        text = stringResource(chat.schildi.lib.R.string.sc_space_list_permissions_missing_for_following),
        color = ElementTheme.colors.textPrimary,
        style = ElementTheme.typography.fontBodyLgMedium,
        textAlign = TextAlign.Left,
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp, start = 4.dp, end = 4.dp),
    )
}
