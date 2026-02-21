package chat.schildi.features.home.spaces

import chat.schildi.lib.preferences.ScAppStateStore
import chat.schildi.lib.preferences.ScPreferencesStore
import chat.schildi.lib.preferences.ScPrefs
import dev.zacsweers.metro.Inject
import io.element.android.features.home.impl.datasource.EXTENDED_VISIBILITY_RANGE_SIZE
import io.element.android.features.home.impl.datasource.PAGINATION_THRESHOLD
import io.element.android.features.home.impl.datasource.RoomListDataSource
import io.element.android.features.home.impl.datasource.SUBSCRIBE_TO_VISIBLE_ROOMS_DEBOUNCE_IN_MILLIS
import io.element.android.features.home.impl.model.RoomListRoomSummary
import io.element.android.libraries.core.coroutine.childScope
import io.element.android.libraries.matrix.api.roomlist.DynamicRoomList
import io.element.android.libraries.matrix.api.roomlist.RoomList
import io.element.android.libraries.matrix.api.roomlist.RoomListFilter
import io.element.android.libraries.matrix.api.roomlist.updateVisibleRange
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

@Inject
class ScRoomListDataSource(
    private val scPreferencesStore: ScPreferencesStore,
    private val roomListDataSource: RoomListDataSource,
) {
    private val tag = "ScRoomsSource"

    private val _spaceSelectionHierarchy = MutableStateFlow<ImmutableList<String>?>(null)
    private val _roomSummariesFlow = MutableSharedFlow<ImmutableList<RoomListRoomSummary>>(replay = 1)
    private val _selectedSpaceItem = MutableStateFlow<SpaceListDataSource.AbstractSpaceHierarchyItem?>(null)

    val unfilteredRoomSummariesFlow = roomListDataSource.roomSummariesFlow
    val spaceSelectionHierarchy: StateFlow<ImmutableList<String>?> = _spaceSelectionHierarchy

    val roomSummariesFlow: SharedFlow<ImmutableList<RoomListRoomSummary>> = _roomSummariesFlow

    private val useScSpaceNav = scPreferencesStore.settingFlow(ScPrefs.SPACE_NAV)

    @OptIn(FlowPreview::class)
    fun launchIn(
        parentCoroutineScope: CoroutineScope,
        spaceListDataSource: SpaceListDataSource,
        scAppStateStore: ScAppStateStore
    ) {
        roomListDataSource.launchIn(parentCoroutineScope)
        var currentChildScope: CoroutineScope? = null
        useScSpaceNav.distinctUntilChanged().onEach { enableScSpaceNav ->
            val coroutineScope = parentCoroutineScope.childScope(Dispatchers.Default, "scRoomListDataSource")
            currentChildScope?.cancel("Updated space nav setting")
            currentChildScope = coroutineScope

            if (!enableScSpaceNav) {
                coroutineScope.launch {
                    _roomSummariesFlow.emitAll(roomListDataSource.roomSummariesFlow)
                }
            } else {
                // Clear any conflicting space filters from upstream spaces implementation
                updateFilter(RoomListFilter.All(emptyList()))
                // Make sure we load all rooms - necessary for unread count calculation, and paging based on visible range is unreliable with kotlin-sided
                // space filters (but we can't use the Rust ones yet, until we have unread counts from there + complete pseudo spaces coverage)
                roomListDataSource.roomList.loadAllIncrementally(coroutineScope)
                // Restore previous space selection
                coroutineScope.launch {
                    _spaceSelectionHierarchy.value = scAppStateStore.loadInitialSpaceSelection().toImmutableList()
                }
                // Persist space selection to restore on next app launch
                _spaceSelectionHierarchy.debounce(2000).onEach { selection ->
                    if (selection == null) {
                        // Not initialized yet
                        return@onEach
                    }
                    scAppStateStore.persistSpaceSelection(selection)
                }.launchIn(coroutineScope)

                // From life space list and current space selection, build the RoomId filter
                combine(
                    _spaceSelectionHierarchy,
                    spaceListDataSource.allSpaces,
                    scPreferencesStore.pseudoSpaceSettingsFlow(),
                ) { spaceSelectionValue, allSpacesValue, pseudoSpaces ->
                    // No space selected or not initialized yet -> show all
                    if (spaceSelectionValue.isNullOrEmpty()) {
                        return@combine Pair(null, true)
                    }
                    // Show all rooms while space list is not loaded yet, without clearing the space hierarchy
                    val spaceList = allSpacesValue?.takeIf { it.isNotEmpty() || pseudoSpaces.hasSpaceIndependentPseudoSpace() } ?: return@combine Pair(null, true)
                    // Resolve actual space from space hierarchy
                    val space = spaceList.resolveSelection(spaceSelectionValue) ?: return@combine Pair(null, false)
                    return@combine Pair(space, true)
                }
                    .onEach { (space, spaceFound) ->
                        _selectedSpaceItem.value = space
                        if (!spaceFound) {
                            Timber.tag(tag).i("Selected space not found")
                            // In the room loads later with paginating sync, may still be useful to not clear.
                            // UI will render home anyway until user selects a different space.
                            //updateSpaceSelection(persistentListOf())
                        }
                    }
                    .launchIn(coroutineScope)
                // Workaround to refresh m.space.child relations without listening to every room's state
                _spaceSelectionHierarchy.filter { !it.isNullOrEmpty() }.drop(1).debounce(500).onEach {
                    spaceListDataSource.forceRebuildSpaceFilter()
                }.launchIn(coroutineScope)

                // Filter by space with the room id list built in the previous flow
                combine(
                    _selectedSpaceItem,
                    roomListDataSource.roomSummariesFlow,
                    scPreferencesStore.settingFlow(ScPrefs.PSEUDO_SPACE_ALL_ROOMS),
                ) { selectedSpace, allRoomsValue, allowAllRooms ->
                    // Do the actual filtering
                    selectedSpace?.applyFilter(allRoomsValue) ?: allRoomsValue.takeIf { allowAllRooms } ?: persistentListOf()
                }
                    .onEach {
                        _roomSummariesFlow.emit(it)
                    }
                    .launchIn(coroutineScope)
            }
        }.launchIn(parentCoroutineScope)
    }

    fun updateSpaceSelection(spaceSelectionHierarchy: ImmutableList<String>) {
        _spaceSelectionHierarchy.value = spaceSelectionHierarchy
    }

    private suspend fun findVisibleRangeInUpstreamSource(range: IntRange): IntRange {
        if (range.isEmpty()) return range
        val currentRoomList = roomSummariesFlow.first()
        if (currentRoomList.isEmpty()) {
            return IntRange.EMPTY
        }
        val upstreamList = roomListDataSource.roomSummariesFlow.first()
        if (upstreamList.isEmpty()) {
            Timber.tag(tag).e("Can't translate room range $range, downstream list has ${currentRoomList.size} entries but upstream is empty")
            return IntRange.EMPTY
        }
        if (upstreamList.size < currentRoomList.size) {
            Timber.tag(tag).w("Downstream list has ${currentRoomList.size} entries but upstream has only ${upstreamList.size}")
        }
        val firstRoom = currentRoomList.getOrNull(range.first.coerceIn(0, currentRoomList.size-1))
        val lastRoom = currentRoomList.getOrNull(range.last.coerceIn(0, currentRoomList.size-1))
        if (firstRoom == null || lastRoom == null) {
            Timber.tag(tag).e("Can't translate room range $range, current list has ${currentRoomList.size} items")
            return range
        }
        val firstRoomIndex = upstreamList.indexOfFirst { it.roomId == firstRoom.roomId }
        val lastRoomIndex = upstreamList.indexOfLast { it.roomId == lastRoom.roomId }
        if (firstRoomIndex == -1 || lastRoomIndex == -1) {
            Timber.tag(tag).e("Can't find all rooms in upstream room list: ${firstRoom.roomId} -> $firstRoomIndex, ${lastRoom.roomId} -> $lastRoomIndex")
            return range
        }
        if (firstRoomIndex > lastRoomIndex) {
            Timber.tag(tag).e("Unexpected room order: ${firstRoom.roomId} -> $firstRoomIndex, ${lastRoom.roomId} -> $lastRoomIndex")
            return IntRange(lastRoomIndex, firstRoomIndex)
        }
        return IntRange(firstRoomIndex, lastRoomIndex).also {
            Timber.tag(tag).v("Mapped visible range: $range -> $it via [${firstRoom.roomId}, ${lastRoom.roomId}], total ${currentRoomList.size}/${upstreamList.size}")
        }
    }

    // Adapted from RoomListDataSource
    suspend fun updateVisibleRange(visibleRange: IntRange) {
        if (!useScSpaceNav.first()) {
            roomListDataSource.updateVisibleRange(visibleRange)
            return
        }
        coroutineScope {
            launch {
                roomListDataSource.roomList.updateVisibleRange(
                    findVisibleRangeInUpstreamSource(visibleRange),
                    PAGINATION_THRESHOLD
                )
            }
            launch {
                subscribeToVisibleRoomsIfNeeded(visibleRange)
            }
        }
    }

    // Adapted from RoomListDataSource
    private var currentSubscribeToVisibleRoomsJob: Job? = null
    private fun CoroutineScope.subscribeToVisibleRoomsIfNeeded(range: IntRange) {
        currentSubscribeToVisibleRoomsJob?.cancel()
        currentSubscribeToVisibleRoomsJob = launch {
            // Debounce the subscription to avoid subscribing to too many rooms
            delay(SUBSCRIBE_TO_VISIBLE_ROOMS_DEBOUNCE_IN_MILLIS)

            if (range.isEmpty()) return@launch
            val currentRoomList = roomSummariesFlow.first()
            // Use extended range to 'prefetch' the next rooms info
            val midExtendedRangeSize = EXTENDED_VISIBILITY_RANGE_SIZE / 2
            val extendedRange = range.first until range.last + midExtendedRangeSize
            val roomIds = extendedRange.mapNotNull { index ->
                currentRoomList.getOrNull(index)?.roomId
            }
            roomListDataSource.roomListService.subscribeToVisibleRooms(roomIds)
        }
    }

    // Some thin roomListDataSource wrappers to make the SC one a drop-in replacement
    val loadingState = roomListDataSource.loadingState
    suspend fun updateFilter(filter: RoomListFilter) {
        if (useScSpaceNav.first()) {
            val cleanedFilter = filter.removeSpaceFilter()
            if (cleanedFilter != filter) {
                Timber.tag(tag).e("Tried to apply upstream space filter while using SC space nav, dropped")
            }
            roomListDataSource.updateFilter(cleanedFilter ?: RoomListFilter.None)
        } else {
            roomListDataSource.updateFilter(filter)
        }
    }

    // When viewing a space, we can't tell for sure if the visible range is sufficient to view all from the current space...
    // We could filter using Rust filters, but still we need all to have reliable unread counts, until we implement that in Rust as well (+ pseudo spaces).
    // Adjusted version of the one lost in upstream merge v26.01.2..v26.02.0
    fun DynamicRoomList.loadAllIncrementally(coroutineScope: CoroutineScope) {
        combine(
            loadingState,
            summaries.map { it.size }.distinctUntilChanged(),
        ) { loadingState, loadedRooms ->
            loadingState to loadedRooms
        }
            .onEach { (loadingState, loadedRooms) ->
                when (loadingState) {
                    is RoomList.LoadingState.Loaded -> {
                        if (loadedRooms < loadingState.numberOfRooms) {
                            Timber.tag(tag).v("loadMore: ${loadedRooms}/${loadingState.numberOfRooms}")
                            loadMore()
                        } else {
                            Timber.tag(tag).v("loadMore done at ${loadedRooms}/${loadingState.numberOfRooms}")
                        }
                    }
                    RoomList.LoadingState.NotLoaded -> Unit
                }
            }
            .launchIn(coroutineScope)
    }
}

private fun RoomListFilter.removeSpaceFilter(): RoomListFilter? = when (this) {
    RoomListFilter.Category.Space -> null
    is RoomListFilter.All -> copy(filters = filters.mapNotNull { it.removeSpaceFilter() })
    is RoomListFilter.Any -> copy(filters = filters.mapNotNull { it.removeSpaceFilter() })
    RoomListFilter.Category.Group,
    RoomListFilter.Category.People,
    RoomListFilter.Favorite,
    is RoomListFilter.Identifiers,
    RoomListFilter.Invite,
    RoomListFilter.None,
    is RoomListFilter.NormalizedMatchRoomName,
    RoomListFilter.Unread -> this
}
