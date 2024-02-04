package chat.schildi.features.roomlist.spaces

import androidx.compose.runtime.Immutable
import chat.schildi.lib.preferences.ScPreferencesStore
import chat.schildi.lib.preferences.ScPrefs
import io.element.android.features.roomlist.impl.datasource.RoomListDataSource
import io.element.android.features.roomlist.impl.model.RoomListRoomSummary
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class SpaceUnreadCountsDataSource @Inject constructor(
    private val scPreferencesStore: ScPreferencesStore
) {

    private val _spaceUnreadCounts = MutableStateFlow<ImmutableMap<String?, SpaceUnreadCounts>>(persistentMapOf())
    val spaceUnreadCounts: StateFlow<ImmutableMap<String?, SpaceUnreadCounts>> = _spaceUnreadCounts

    fun launchIn(
        coroutineScope: CoroutineScope,
        roomListDataSource: RoomListDataSource,
        spaceAwareRoomListDataSource: SpaceAwareRoomListDataSource,
        spaceListDataSource: SpaceListDataSource
    ) {
        combine(
            roomListDataSource.allRooms,
            spaceListDataSource.allSpaces,
            spaceAwareRoomListDataSource.spaceSelectionHierarchy,
            scPreferencesStore.settingFlow(ScPrefs.CLIENT_GENERATED_UNREAD_COUNTS),
        ) { allRoomsValue, rootSpaces, spaceSelectionValue, useClientGeneratedCounts ->
            rootSpaces ?: return@combine mapOf()
            spaceSelectionValue ?: return@combine mapOf()
            val visibleSpaces = if (spaceSelectionValue.isEmpty()) {
                // Nothing selected, only root spaces visible
                rootSpaces
            } else {
                // When a space is selected, either its children or its siblings and parent can be visible.
                // Here, the space's "siblings" contains the selected space itself as well.
                // Root spaces can be visible as well in the case of compact app bar.
                val children = rootSpaces.resolveSelection(spaceSelectionValue)?.spaces.orEmpty()
                val parent = rootSpaces.resolveSelection(spaceSelectionValue.subList(0, spaceSelectionValue.size-1))
                val siblings = parent?.spaces ?: emptyList()
                rootSpaces + siblings + children + parent?.let { if (rootSpaces.contains(parent)) emptyList() else listOf(it) }.orEmpty()
            }
            val result = mutableMapOf<String?, SpaceUnreadCounts>(
                // Total count
                null to getAggregatedUnreadCounts(allRoomsValue, useClientGeneratedCounts)
            )
            visibleSpaces.forEach { result[it.info.roomId.value] = getUnreadCountsForSpace(it, allRoomsValue, useClientGeneratedCounts) }
            result
        }.onEach {
            _spaceUnreadCounts.emit(it.toImmutableMap())
        }.launchIn(coroutineScope)
    }

    private fun getUnreadCountsForSpace(
        space: SpaceListDataSource.SpaceHierarchyItem,
        allRooms: List<RoomListRoomSummary>,
        useClientGeneratedUnreadCounts: Boolean,
    ) = getAggregatedUnreadCounts(
        allRooms.filter { space.flattenedRooms.contains(it.roomId.value) },
        useClientGeneratedUnreadCounts,
    )

    private fun getAggregatedUnreadCounts(rooms: List<RoomListRoomSummary>, useClientGeneratedUnreadCounts: Boolean): SpaceUnreadCounts {
        var unread = SpaceUnreadCounts()
        for (room in rooms) {
            unread = unread.add(
                if (useClientGeneratedUnreadCounts) room.numberOfUnreadMentions else room.highlightCount,
                if (useClientGeneratedUnreadCounts) room.numberOfUnreadNotifications else room.notificationCount,
                if (useClientGeneratedUnreadCounts) room.numberOfUnreadMessages else room.unreadCount,
                room.markedUnread,
            )
        }
        return unread
    }

    private fun SpaceUnreadCounts.add(mentions: Int, notifications: Int, unread: Int, markedUnread: Boolean): SpaceUnreadCounts = SpaceUnreadCounts(
        this.mentionedMessages + mentions,
        this.notifiedMessages + notifications,
        this.unreadMessages + unread,
        this.mentionedChats + if (mentions > 0) 1 else 0,
        this.notifiedChats + if (notifications > 0) 1 else 0,
        this.unreadChats + if (unread > 0) 1 else 0,
        this.markedUnreadChats + if (markedUnread) 1 else 0
    )

    @Immutable
    data class SpaceUnreadCounts(
        val mentionedMessages: Int = 0,
        val notifiedMessages: Int = 0,
        val unreadMessages: Int = 0,
        val mentionedChats: Int = 0,
        val notifiedChats: Int = 0,
        val unreadChats: Int = 0,
        val markedUnreadChats: Int = 0,
    )
}
