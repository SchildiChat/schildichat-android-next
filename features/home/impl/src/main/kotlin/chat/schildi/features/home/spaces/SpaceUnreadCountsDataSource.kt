package chat.schildi.features.home.spaces

import androidx.compose.runtime.Immutable
import chat.schildi.lib.preferences.ScPreferencesStore
import chat.schildi.lib.preferences.ScPrefs
import dev.zacsweers.metro.Inject
import io.element.android.features.home.impl.datasource.RoomListDataSource
import io.element.android.features.home.impl.model.RoomListRoomSummary
import io.element.android.features.home.impl.model.RoomSummaryDisplayType
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Inject
class SpaceUnreadCountsDataSource(
    private val scPreferencesStore: ScPreferencesStore,
    private val roomListService: RoomListService,
) {

    private val _enrichedSpaces = MutableStateFlow<ImmutableList<SpaceListDataSource.AbstractSpaceHierarchyItem>?>(null)
    val enrichedSpaces: StateFlow<ImmutableList<SpaceListDataSource.AbstractSpaceHierarchyItem>?> = _enrichedSpaces
    private val _totalUnreadCounts = MutableStateFlow<SpaceUnreadCounts?>(null)
    val totalUnreadCounts: StateFlow<SpaceUnreadCounts?> = _totalUnreadCounts

    fun launchIn(
        coroutineScope: CoroutineScope,
        roomListDataSource: RoomListDataSource,
        spaceAwareRoomListDataSource: SpaceAwareRoomListDataSource,
        spaceListDataSource: SpaceListDataSource
    ) {
        combine(
            roomListDataSource.allRooms.throttleLatest(300),
            spaceListDataSource.allSpaces,
            spaceAwareRoomListDataSource.spaceSelectionHierarchy,
            scPreferencesStore.settingFlow(ScPrefs.CLIENT_GENERATED_UNREAD_COUNTS),
            scPreferencesStore.settingFlow(ScPrefs.SPACE_NAV),
        ) { allRoomsValue, rootSpaces, spaceSelectionValue, useClientGeneratedCounts, spaceNavEnabled ->
            if (!spaceNavEnabled || rootSpaces == null || spaceSelectionValue == null) {
                return@combine Triple(SpaceUnreadCounts(), null, emptyList())
            }
            val visibleSpaces = if (spaceSelectionValue.isEmpty()) {
                // Nothing selected, only root spaces visible
                rootSpaces
            } else {
                // When a space is selected, either its children or its siblings and parent can be visible.
                // Here, the space's "siblings" contains the selected space itself as well.
                // Root spaces can be visible as well in the case of compact app bar.
                val children = (rootSpaces.resolveSelection(spaceSelectionValue) as? SpaceListDataSource.SpaceHierarchyItem)?.spaces.orEmpty()
                val parent = rootSpaces.resolveSelection(spaceSelectionValue.subList(0, spaceSelectionValue.size-1))
                val siblings = (parent as? SpaceListDataSource.SpaceHierarchyItem)?.spaces.orEmpty()
                rootSpaces + siblings + children + parent?.let { if (rootSpaces.contains(parent)) emptyList() else listOf(it) }.orEmpty()
            }
            val totalUnreadCount = getAggregatedUnreadCounts(allRoomsValue, useClientGeneratedCounts)
            val visibleSpaceIds = visibleSpaces.mapNotNull { (it as? SpaceListDataSource.SpaceHierarchyItem)?.info?.roomId }
            val newEnrichedSpaces = rootSpaces.map { space ->
                space.enrich {
                    if (it in visibleSpaces) getUnreadCountsForSpace(it, allRoomsValue, useClientGeneratedCounts) else null
                }
            }.toImmutableList()
            //visibleSpaces.forEach { result[it.selectionId] = getUnreadCountsForSpace(it, allRoomsValue, useClientGeneratedCounts) }
            Triple(totalUnreadCount, newEnrichedSpaces, visibleSpaceIds)
        }.onEach { (totalUnreadCount, result, visibleSpaceIds) ->
            _totalUnreadCounts.emit(totalUnreadCount)
            _enrichedSpaces.emit(result)
            roomListService.subscribeToVisibleRooms(visibleSpaceIds)
        }.launchIn(coroutineScope)
    }

    private fun getUnreadCountsForSpace(
        space: SpaceListDataSource.AbstractSpaceHierarchyItem,
        allRooms: List<RoomListRoomSummary>,
        useClientGeneratedUnreadCounts: Boolean,
    ) = getAggregatedUnreadCounts(
        space.applyFilter(allRooms),
        useClientGeneratedUnreadCounts,
    )

    private fun getAggregatedUnreadCounts(rooms: List<RoomListRoomSummary>, useClientGeneratedUnreadCounts: Boolean): SpaceUnreadCounts {
        var unread = SpaceUnreadCounts()
        for (room in rooms) {
            unread = unread.add(
                if (useClientGeneratedUnreadCounts) room.numberOfUnreadMentions else room.highlightCount,
                if (useClientGeneratedUnreadCounts) room.numberOfUnreadNotifications else room.notificationCount,
                if (useClientGeneratedUnreadCounts) room.numberOfUnreadMessages else room.unreadCount,
                room.isMarkedUnread,
                room.displayType == RoomSummaryDisplayType.INVITE,
            )
        }
        return unread
    }

    private fun SpaceUnreadCounts.add(
        mentions: Long,
        notifications: Long,
        unread: Long,
        markedUnread: Boolean,
        isInvite: Boolean
    ): SpaceUnreadCounts = if (isInvite) {
        copy(
            notifiedMessages = this.notifiedMessages + 1,
            unreadMessages = this.unreadMessages + 1,
            notifiedChats = this.notifiedChats + 1,
            unreadChats = this.unreadChats + 1,
            inviteCount = this.inviteCount + 1,
        )
    } else {
        SpaceUnreadCounts(
            this.mentionedMessages + mentions,
            this.notifiedMessages + notifications,
            this.unreadMessages + unread,
            this.mentionedChats + if (mentions > 0) 1 else 0,
            this.notifiedChats + if (notifications > 0) 1 else 0,
            this.unreadChats + if (unread > 0) 1 else 0,
            this.markedUnreadChats + if (markedUnread) 1 else 0,
            this.inviteCount,
        )
    }

    @Immutable
    data class SpaceUnreadCounts(
        val mentionedMessages: Long = 0,
        val notifiedMessages: Long = 0,
        val unreadMessages: Long = 0,
        val mentionedChats: Long = 0,
        val notifiedChats: Long = 0,
        val unreadChats: Long = 0,
        val markedUnreadChats: Long = 0,
        val inviteCount: Long = 0,
    )
}

// Emit immediately but delay too fast updates after that
fun <T> Flow<T>.throttleLatest(period: Long) = flow {
    conflate().collect {
        emit(it)
        delay(period)
    }
}
