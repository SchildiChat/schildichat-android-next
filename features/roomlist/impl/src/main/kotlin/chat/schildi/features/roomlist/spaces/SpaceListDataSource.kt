package chat.schildi.features.roomlist.spaces

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Rocket
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tag
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import chat.schildi.lib.preferences.ScPreferencesStore
import chat.schildi.lib.preferences.ScPrefs
import chat.schildi.matrixsdk.ROOM_ACCOUNT_DATA_SPACE_ORDER
import chat.schildi.matrixsdk.SpaceOrderSerializer
import io.element.android.features.roomlist.impl.datasource.RoomListRoomSummaryFactory
import io.element.android.features.roomlist.impl.model.RoomListRoomSummary
import io.element.android.libraries.androidutils.diff.DiffCacheUpdater
import io.element.android.libraries.androidutils.diff.MutableListDiffCache
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.room.MatrixSpaceChildInfo
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

private const val REAL_SPACE_ID_PREFIX = "s:"
private const val PSEUDO_SPACE_ID_PREFIX = "p:"

class SpaceListDataSource @Inject constructor(
    private val client: MatrixClient,
    private val roomListService: RoomListService,
    private val roomListRoomSummaryFactory: RoomListRoomSummaryFactory,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val scPreferencesStore: ScPreferencesStore,
    @ApplicationContext
    private val context: Context,
) {
    private val _allSpaces = MutableStateFlow<ImmutableList<AbstractSpaceHierarchyItem>?>(null)
    private val _forceRebuildFlow = MutableStateFlow(System.currentTimeMillis())

    private val lock = Mutex()
    private val diffCache = MutableListDiffCache<RoomListRoomSummary>()
    private val diffCacheUpdater = DiffCacheUpdater<RoomSummary, RoomListRoomSummary>(diffCache = diffCache, detectMoves = true) { old, new ->
        old?.identifier() == new?.identifier()
    }

    fun launchIn(coroutineScope: CoroutineScope) {
        combine(
            roomListService.allSpaces.summaries,
            scPreferencesStore.pseudoSpaceSettingsFlow(),
            _forceRebuildFlow,
        ) { roomSummaries, pseudoSpaces, _ ->
            Timber.v("Rebuild space list")
            replaceWith(roomSummaries, pseudoSpaces)
        }.launchIn(coroutineScope)
    }

    val allSpaces: StateFlow<ImmutableList<AbstractSpaceHierarchyItem>?> = _allSpaces

    private suspend fun replaceWith(
        roomSummaries: List<RoomSummary>,
        pseudoSpaces: PseudoSpaceSettings,
    ) = withContext(coroutineDispatchers.computation) {
        lock.withLock {
            diffCacheUpdater.updateWith(roomSummaries)
            buildAndEmitAllSpaces(roomSummaries, pseudoSpaces)
        }
    }

    private suspend fun buildAndEmitAllSpaces(
        spaceSummaries: List<RoomSummary>,
        pseudoSpaceSettings: PseudoSpaceSettings,
    ) {
        val spaceListRoomSummaries = if (diffCache.isEmpty()) {
            emptyList()
        } else {
            val spaceListRoomSummaries = ArrayList<RoomListRoomSummary>()
            for (index in diffCache.indices()) {
                val cacheItem = diffCache.get(index)
                if (cacheItem == null) {
                    buildAndCacheItem(spaceSummaries, index)?.also { timelineItemState ->
                        spaceListRoomSummaries.add(timelineItemState)
                    }
                } else {
                    spaceListRoomSummaries.add(cacheItem)
                }
            }
            spaceListRoomSummaries
        }
        val pseudoSpaces = mutableListOf<PseudoSpaceItem>()
        if (pseudoSpaceSettings.favorites) {
            pseudoSpaces.add(
                FavoritesPseudoSpaceItem(context.getString(chat.schildi.lib.R.string.sc_pseudo_space_favorites))
            )
        }
        if (pseudoSpaceSettings.dms) {
            pseudoSpaces.add(
                DmsPseudoSpaceItem(context.getString(chat.schildi.lib.R.string.sc_pseudo_space_dms))
            )
        }
        if (pseudoSpaceSettings.groups) {
            pseudoSpaces.add(
                GroupsPseudoSpaceItem(context.getString(chat.schildi.lib.R.string.sc_pseudo_space_groups))
            )
        }
        if (pseudoSpaceSettings.spaceless || pseudoSpaceSettings.spacelessGroups) {
            val excludedRooms = spaceSummaries.flatMap { (it as? RoomSummary.Filled)?.details?.spaceChildren?.map { it.roomId }.orEmpty() }.toImmutableList()
            if (pseudoSpaceSettings.spacelessGroups) {
                pseudoSpaces.add(
                    SpacelessGroupsPseudoSpaceItem(context.getString(chat.schildi.lib.R.string.sc_pseudo_space_spaceless_groups_short), excludedRooms)
                )
            }
            if (pseudoSpaceSettings.spaceless) {
                pseudoSpaces.add(
                    SpacelessPseudoSpaceItem(
                        context.getString(chat.schildi.lib.R.string.sc_pseudo_space_spaceless_short),
                        excludedRooms,
                        pseudoSpaceSettings.spacelessGroups
                    )
                )
            }
        }
        _allSpaces.emit(buildSpaceHierarchy(spaceListRoomSummaries, pseudoSpaces))
    }

    // Force rebuilding a space filter. Only a workaround until we can do proper listener to m.space.child state events...
    suspend fun forceRebuildSpaceFilter() {
        _forceRebuildFlow.emit(System.currentTimeMillis())
    }

    /**
     * Build the space hierarchy and avoid loops
     */
    // TODO what can we cache something here?
    private suspend fun buildSpaceHierarchy(spaceSummaries: List<RoomListRoomSummary>, pseudoSpaces: List<PseudoSpaceItem>): ImmutableList<AbstractSpaceHierarchyItem> {
        // Map spaceId -> list of child spaces
        val spaceHierarchyMap = HashMap<String, MutableList<Pair<MatrixSpaceChildInfo, RoomListRoomSummary>>>()
        // Map spaceId -> list of regular child rooms
        val regularChildren = HashMap<String, MutableList<MatrixSpaceChildInfo>>()
        val rootSpaces = HashSet<RoomListRoomSummary>(spaceSummaries)
        spaceSummaries.forEach { parentSpace ->
            parentSpace.spaceChildren.forEach childLoop@{ spaceChild ->
                val child = spaceSummaries.find { it.roomId.value == spaceChild.roomId }
                if (child == null) {
                    // Treat as regular child, since it doesn't appear to be a space (at least none known to us at this point)
                    regularChildren[parentSpace.roomId.value] =
                        regularChildren[parentSpace.roomId.value]?.apply { add(spaceChild) } ?: mutableListOf(spaceChild)
                    return@childLoop
                }
                rootSpaces.removeAll { it.roomId.value == spaceChild.roomId }
                spaceHierarchyMap[parentSpace.roomId.value] = spaceHierarchyMap[parentSpace.roomId.value]?.apply {
                    add(Pair(spaceChild, child))
                } ?: mutableListOf(Pair(spaceChild, child))
            }
        }

        // Build the actual immutable recursive data structures that replicate the hierarchy
        return (pseudoSpaces + rootSpaces.map {
            val order = client.getRoomAccountData(it.roomId, ROOM_ACCOUNT_DATA_SPACE_ORDER)
                ?.let { SpaceOrderSerializer.deserializeContent(it) }?.getOrNull()?.order
            createSpaceHierarchyItem(it, order, spaceHierarchyMap, regularChildren)
        }.sortedWith(SpaceComparator)).toImmutableList()
    }

    private fun createSpaceHierarchyItem(
        spaceSummary: RoomListRoomSummary,
        order: String?,
        hierarchy: HashMap<String, MutableList<Pair<MatrixSpaceChildInfo, RoomListRoomSummary>>>,
        regularChildren: HashMap<String, MutableList<MatrixSpaceChildInfo>>,
        forbiddenChildren: List<String> = emptyList(),
    ): SpaceHierarchyItem {
        val children = hierarchy[spaceSummary.id]?.mapNotNull { (spaceChildInfo, child) ->
            if (child.roomId.value in forbiddenChildren) {
                Timber.w("Detected space loop: ${spaceSummary.id} -> ${child.roomId.value}")
                null
            } else {
                createSpaceHierarchyItem(child, spaceChildInfo.order, hierarchy, regularChildren, forbiddenChildren + listOf(spaceSummary.roomId.value))
            }
        }?.sortedWith(SpaceComparator)?.toImmutableList() ?: persistentListOf()
        return SpaceHierarchyItem(
            info = spaceSummary,
            order = order,
            spaces = children,
            flattenedRooms = (
                // All direct children rooms
                regularChildren[spaceSummary.id].orEmpty().map { it.roomId }
                    // All indirect children rooms
                    + children.flatMap { it.flattenedRooms }
                ).toImmutableList(),
            // This and all children spaces
            flattenedSpaces = (listOf(spaceSummary.roomId.value) + children.flatMap { it.flattenedSpaces }).toImmutableList()
        )
    }

    private fun buildAndCacheItem(roomSummaries: List<RoomSummary>, index: Int): RoomListRoomSummary? {
        val roomListRoomSummary = when (val roomSummary = roomSummaries.getOrNull(index)) {
            is RoomSummary.Empty -> RoomListRoomSummaryFactory.createPlaceholder(roomSummary.identifier)
            is RoomSummary.Filled -> roomListRoomSummaryFactory.create(roomSummary)
            null -> null
        }
        diffCache[index] = roomListRoomSummary
        return roomListRoomSummary
    }

    @Immutable
    sealed interface AbstractSpaceHierarchyItem {
        val name: String
        val selectionId: String
        val spaces: ImmutableList<SpaceHierarchyItem>
        fun applyFilter(rooms: List<RoomListRoomSummary>): ImmutableList<RoomListRoomSummary>
    }

    @Immutable
    data class SpaceHierarchyItem(
        val info: RoomListRoomSummary,
        val order: String?,
        override val spaces: ImmutableList<SpaceHierarchyItem>,
        val flattenedRooms: ImmutableList<String>,
        val flattenedSpaces: ImmutableList<String>,
    ) : AbstractSpaceHierarchyItem {
        override val name = info.name
        override val selectionId = "$REAL_SPACE_ID_PREFIX${info.roomId.value}"

        override fun applyFilter(rooms: List<RoomListRoomSummary>) = rooms.filter { flattenedRooms.contains(it.roomId.value) }.toImmutableList()
    }

    @Immutable
    abstract class PseudoSpaceItem(
        val id: String,
        val icon: ImageVector,
    ) : AbstractSpaceHierarchyItem {
        override val selectionId = "$PSEUDO_SPACE_ID_PREFIX$id"
        override val spaces = persistentListOf<SpaceHierarchyItem>()
    }

    @Immutable
    data class FavoritesPseudoSpaceItem(override val name: String) : PseudoSpaceItem(
        "fav",
        Icons.Default.Star,
    ) {
        override fun applyFilter(rooms: List<RoomListRoomSummary>) =
            rooms.filter { it.isFavorite }.toImmutableList()
    }

    @Immutable
    data class DmsPseudoSpaceItem(override val name: String) : PseudoSpaceItem(
        "dm",
        Icons.Default.Person,
    ) {
        override fun applyFilter(rooms: List<RoomListRoomSummary>) =
            rooms.filter { it.isDm }.toImmutableList()
    }

    @Immutable
    data class GroupsPseudoSpaceItem(override val name: String) : PseudoSpaceItem(
        "group",
        Icons.Default.Groups,
    ) {
        override fun applyFilter(rooms: List<RoomListRoomSummary>) =
            rooms.filter { !it.isDm }.toImmutableList()
    }

    @Immutable
    data class SpacelessGroupsPseudoSpaceItem(override val name: String, val excludedRooms: ImmutableList<String>) : PseudoSpaceItem(
        "spaceless/group",
        Icons.Default.Tag,
    ) {
        override fun applyFilter(rooms: List<RoomListRoomSummary>) =
            rooms.filter { !it.isDm && !excludedRooms.contains(it.roomId.value) }.toImmutableList()
    }

    @Immutable
    data class SpacelessPseudoSpaceItem(
        override val name: String,
        val excludedRooms: ImmutableList<String>,
        val conflictsWithSpacelessGroups: Boolean
    ) : PseudoSpaceItem(
        "spaceless",
        if (conflictsWithSpacelessGroups) Icons.Default.Rocket else Icons.Default.Tag,
    ) {
        override fun applyFilter(rooms: List<RoomListRoomSummary>) =
            rooms.filter { !excludedRooms.contains(it.roomId.value) }.toImmutableList()
    }

    data class PseudoSpaceSettings(
        val favorites: Boolean,
        val dms: Boolean,
        val groups: Boolean,
        val spacelessGroups: Boolean,
        val spaceless: Boolean,
    ) {
        fun hasSpaceIndependentPseudoSpace() = favorites || dms || groups
    }
}

fun ScPreferencesStore.pseudoSpaceSettingsFlow(): Flow<SpaceListDataSource.PseudoSpaceSettings> {
    return combinedSettingFlow { lookup ->
        SpaceListDataSource.PseudoSpaceSettings(
            favorites = ScPrefs.PSEUDO_SPACE_FAVORITES.let { it.ensureType(lookup(it)) ?: it.defaultValue },
            dms = ScPrefs.PSEUDO_SPACE_DMS.let { it.ensureType(lookup(it)) ?: it.defaultValue },
            groups = ScPrefs.PSEUDO_SPACE_GROUPS.let { it.ensureType(lookup(it)) ?: it.defaultValue },
            spacelessGroups = ScPrefs.PSEUDO_SPACE_SPACELESS_GROUPS.let { it.ensureType(lookup(it)) ?: it.defaultValue },
            spaceless = ScPrefs.PSEUDO_SPACE_SPACELESS.let { it.ensureType(lookup(it)) ?: it.defaultValue },
        )
    }
}

fun List<SpaceListDataSource.AbstractSpaceHierarchyItem>.resolveSelection(selection: List<String>): SpaceListDataSource.AbstractSpaceHierarchyItem? {
    var space: SpaceListDataSource.AbstractSpaceHierarchyItem? = null
    var spaceList = this
    selection.forEach { spaceId ->
        space = spaceList.find { it.selectionId == spaceId }
        if (space == null) {
            return null
        }
        spaceList = (space as? SpaceListDataSource.SpaceHierarchyItem)?.spaces.orEmpty()
    }
    return space
}

fun isSpaceFilterActive(selection: List<String>): Boolean {
    // No need to resolveSelection() the whole hierarchy, checking the first selection is enough
    return selection.firstOrNull()?.startsWith(REAL_SPACE_ID_PREFIX) == true
}

@Composable
fun List<SpaceListDataSource.AbstractSpaceHierarchyItem>.resolveSpaceName(selection: List<String>): String? {
    // if this.isEmpty(), spaces are disabled, in which case we want to return null
    if (isEmpty()) {
        return null
    }
    return resolveSelection(selection)?.name ?: stringResource(chat.schildi.lib.R.string.sc_space_all_rooms_title)
}
