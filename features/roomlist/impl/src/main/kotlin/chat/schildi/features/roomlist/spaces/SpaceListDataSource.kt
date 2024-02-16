package chat.schildi.features.roomlist.spaces

import androidx.compose.runtime.Immutable
import io.element.android.features.roomlist.impl.datasource.RoomListRoomSummaryFactory
import io.element.android.features.roomlist.impl.model.RoomListRoomSummary
import io.element.android.libraries.androidutils.diff.DiffCacheUpdater
import io.element.android.libraries.androidutils.diff.MutableListDiffCache
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.MatrixSpaceChildInfo
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class SpaceListDataSource @Inject constructor(
    private val client: MatrixClient,
    private val roomListService: RoomListService,
    private val roomListRoomSummaryFactory: RoomListRoomSummaryFactory,
    private val coroutineDispatchers: CoroutineDispatchers,
) {
    private val _allSpaces = MutableStateFlow<ImmutableList<SpaceHierarchyItem>?>(null)

    private val lock = Mutex()
    private val diffCache = MutableListDiffCache<RoomListRoomSummary>()
    private val diffCacheUpdater = DiffCacheUpdater<RoomSummary, RoomListRoomSummary>(diffCache = diffCache, detectMoves = true) { old, new ->
        old?.identifier() == new?.identifier()
    }

    fun launchIn(coroutineScope: CoroutineScope) {
        roomListService
            .allSpaces
            .summaries
            .onEach { roomSummaries ->
                replaceWith(roomSummaries)
            }
            .launchIn(coroutineScope)
    }

    val allSpaces: StateFlow<ImmutableList<SpaceHierarchyItem>?> = _allSpaces

    private suspend fun replaceWith(roomSummaries: List<RoomSummary>) = withContext(coroutineDispatchers.computation) {
        lock.withLock {
            diffCacheUpdater.updateWith(roomSummaries)
            buildAndEmitAllSpaces(roomSummaries)
        }
    }

    private suspend fun buildAndEmitAllSpaces(roomSummaries: List<RoomSummary>) {
        if (diffCache.isEmpty()) {
            _allSpaces.emit(persistentListOf())
        } else {
            val roomListRoomSummaries = ArrayList<RoomListRoomSummary>()
            for (index in diffCache.indices()) {
                val cacheItem = diffCache.get(index)
                if (cacheItem == null) {
                    buildAndCacheItem(roomSummaries, index)?.also { timelineItemState ->
                        roomListRoomSummaries.add(timelineItemState)
                    }
                } else {
                    roomListRoomSummaries.add(cacheItem)
                }
            }
            _allSpaces.emit(buildSpaceHierarchy(roomListRoomSummaries))
        }
    }

    // Force rebuilding a space filter. Only a workaround until we can do proper listener to m.space.child state events...
    suspend fun forceRebuildSpaceFilter() {
        replaceWith(roomListService.allSpaces.summaries.value)
    }

    /**
     * Build the space hierarchy and avoid loops
     */
    // TODO what can we cache something here?
    private suspend fun buildSpaceHierarchy(spaceSummaries: List<RoomListRoomSummary>): ImmutableList<SpaceHierarchyItem> {
        // Map spaceId -> list of child spaces
        val spaceHierarchyMap = HashMap<String, MutableList<Pair<MatrixSpaceChildInfo, RoomListRoomSummary>>>()
        // Map spaceId -> list of regular child rooms
        val regularChildren = HashMap<String, MutableList<MatrixSpaceChildInfo>>()
        val rootSpaces = HashSet<RoomListRoomSummary>(spaceSummaries)
        spaceSummaries.forEach { parentSpace ->
            val spaceInfo = client.getRoom(parentSpace.roomId)
            val spaceChildren = spaceInfo?.spaceChildren
            spaceChildren?.forEach childLoop@{ spaceChild ->
                val child = spaceSummaries.find { it.roomId.value == spaceChild.roomId }
                if (child == null) {
                    // Treat as regular child, since it doesn't appear to be a space (at least none known to us at this point)
                    regularChildren[parentSpace.roomId.value] = regularChildren[parentSpace.roomId.value]?.apply { add(spaceChild) } ?: mutableListOf(spaceChild)
                    return@childLoop
                }
                rootSpaces.removeAll { it.roomId.value == spaceChild.roomId }
                spaceHierarchyMap[parentSpace.roomId.value] = spaceHierarchyMap[parentSpace.roomId.value]?.apply {
                    add(Pair(spaceChild, child))
                } ?: mutableListOf(Pair(spaceChild, child))
            }
        }

        // Build the actual immutable recursive data structures that replicate the hierarchy
        return rootSpaces.map {
            val order = client.getRoom(it.roomId)?.rootSpaceOrder
            createSpaceHierarchyItem(it, order, spaceHierarchyMap, regularChildren)
        }.sortedWith(SpaceComparator).toImmutableList()
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
    data class SpaceHierarchyItem(
        val info: RoomListRoomSummary,
        val order: String?,
        val spaces: ImmutableList<SpaceHierarchyItem>,
        val flattenedRooms: ImmutableList<String>,
        val flattenedSpaces: ImmutableList<String>,
    )
}

fun List<SpaceListDataSource.SpaceHierarchyItem>.resolveSelection(selection: List<String>): SpaceListDataSource.SpaceHierarchyItem? {
    var space: SpaceListDataSource.SpaceHierarchyItem? = null
    var spaceList = this
    selection.forEach { spaceId ->
        space = spaceList.find { it.info.roomId.value == spaceId }
        if (space == null) {
            return null
        }
        spaceList = space!!.spaces
    }
    return space
}
