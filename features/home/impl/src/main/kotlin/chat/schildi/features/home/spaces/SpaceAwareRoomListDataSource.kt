package chat.schildi.features.home.spaces

import chat.schildi.lib.preferences.ScAppStateStore
import chat.schildi.lib.preferences.ScPreferencesStore
import chat.schildi.lib.preferences.ScPrefs
import dev.zacsweers.metro.Inject
import io.element.android.features.home.impl.datasource.RoomListDataSource
import io.element.android.features.home.impl.model.RoomListRoomSummary
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

@Inject
class SpaceAwareRoomListDataSource(
    private val scPreferencesStore: ScPreferencesStore,
) {

    private val _spaceSelectionHierarchy = MutableStateFlow<ImmutableList<String>?>(null)
    private val _spaceRooms = MutableSharedFlow<ImmutableList<RoomListRoomSummary>>(replay = 1)
    private val _selectedSpaceItem = MutableStateFlow<SpaceListDataSource.AbstractSpaceHierarchyItem?>(null)

    val spaceRooms: SharedFlow<ImmutableList<RoomListRoomSummary>> = _spaceRooms
    val spaceSelectionHierarchy: StateFlow<ImmutableList<String>?> = _spaceSelectionHierarchy

    @OptIn(FlowPreview::class)
    fun launchIn(
        coroutineScope: CoroutineScope,
        roomListDataSource: RoomListDataSource,
        spaceListDataSource: SpaceListDataSource,
        scAppStateStore: ScAppStateStore
    ) {
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
                    Timber.i("Selected space not found")
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
            roomListDataSource.allRooms,
            scPreferencesStore.settingFlow(ScPrefs.PSEUDO_SPACE_ALL_ROOMS),
        ) { selectedSpace, allRoomsValue, allowAllRooms ->
            // Do the actual filtering
            selectedSpace?.applyFilter(allRoomsValue) ?: allRoomsValue.takeIf { allowAllRooms } ?: persistentListOf()
        }
            .onEach {
                _spaceRooms.emit(it)
            }
            .launchIn(coroutineScope)
    }

    fun updateSpaceSelection(spaceSelectionHierarchy: ImmutableList<String>) {
        _spaceSelectionHierarchy.value = spaceSelectionHierarchy
    }
}
