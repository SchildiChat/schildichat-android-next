package chat.schildi.features.roomlist.spaces

import chat.schildi.lib.preferences.ScAppStateStore
import io.element.android.features.roomlist.impl.datasource.RoomListDataSource
import io.element.android.features.roomlist.impl.model.RoomListRoomSummary
import io.element.android.libraries.matrix.api.MatrixClient
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class SpaceAwareRoomListDataSource @Inject constructor(
    private val client: MatrixClient,
) {

    private val _spaceSelectionHierarchy = MutableStateFlow<ImmutableList<String>?>(null)
    private val _spaceChildFilter = MutableStateFlow<ImmutableList<String>?>(null)
    private val _spaceRooms = MutableStateFlow<ImmutableList<RoomListRoomSummary>>(persistentListOf())
    private val _selectedSpaceItem = MutableStateFlow<SpaceListDataSource.SpaceHierarchyItem?>(null)

    val spaceRooms: StateFlow<ImmutableList<RoomListRoomSummary>> = _spaceRooms
    val spaceSelectionHierarchy: StateFlow<ImmutableList<String>?> = _spaceSelectionHierarchy

    @OptIn(FlowPreview::class)
    fun launchIn(coroutineScope: CoroutineScope, roomListDataSource: RoomListDataSource, spaceListDataSource: SpaceListDataSource, scAppStateStore: ScAppStateStore) {
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
        ) { spaceSelectionValue, allSpacesValue ->
            // No space selected or not initialized yet -> show all
            if (spaceSelectionValue.isNullOrEmpty()) {
                return@combine Pair(null, true)
            }
            // Show all rooms while space list is not loaded yet, without clearing the space hierarchy
            val spaceList = allSpacesValue?.takeIf { it.isNotEmpty() } ?: return@combine Pair(null, true)
            // Resolve actual space from space hierarchy
            val space = spaceList.resolveSelection(spaceSelectionValue) ?: return@combine Pair(null, false)
            return@combine Pair(space, true)
        }
            .onEach { (space, spaceFound) ->
                _selectedSpaceItem.value = space
                _spaceChildFilter.value = space?.flattenedRooms
                if (!spaceFound) {
                    Timber.i("Selected space not found, clearing selection")
                    updateSpaceSelection(persistentListOf())
                }
            }
            .launchIn(coroutineScope)
        // Workaround to refresh m.space.child relations without listening to every room's state
        _spaceSelectionHierarchy.filter { !it.isNullOrEmpty() }.drop(1).debounce(500).onEach {
            spaceListDataSource.forceRebuildSpaceFilter()
        }.launchIn(coroutineScope)

        // Tell SDK we filter the sliding sync window by spaces
        _selectedSpaceItem.debounce(1000).onEach {
            it ?: return@onEach
            Timber.v("Pass space selection to SDK")
            client.roomListService.updateVisibleSpaces(it.flattenedSpaces)
        }.launchIn(coroutineScope)

        // Filter by space with the room id list built in the previous flow
        combine(
            _spaceChildFilter,
           roomListDataSource.allRooms
        ) { filterValue, allRoomsValue ->
            // Do the actual filtering
            when (filterValue) {
                null -> allRoomsValue
                else -> allRoomsValue.filter { filterValue.contains(it.roomId.value) }
            }.toImmutableList()
        }
            .onEach {
                _spaceRooms.value = it
            }
            .launchIn(coroutineScope)
    }

    fun updateSpaceSelection(spaceSelectionHierarchy: ImmutableList<String>) {
        _spaceSelectionHierarchy.value = spaceSelectionHierarchy
    }
}
