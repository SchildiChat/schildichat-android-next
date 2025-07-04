package chat.schildi.screenshots

import android.graphics.Typeface
import android.text.style.StyleSpan
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import chat.schildi.features.home.spaces.SpaceListDataSource
import chat.schildi.features.home.spaces.SpaceUnreadCountsDataSource
import io.element.android.features.leaveroom.api.LeaveRoomState
import io.element.android.features.roomlist.impl.InvitesState
import io.element.android.features.roomlist.impl.RoomListState
import io.element.android.features.roomlist.impl.RoomListView
import io.element.android.features.roomlist.impl.model.RoomListRoomSummary
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.api.user.MatrixUser
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf


@ScScreenshot
@Composable
internal fun RoomListViewPreview(@PreviewParameter(ScRoomListStateProvider::class) state: RoomListState) = ScScreenshot {
    RoomListView(
        state = state,
        onRoomClicked = {},
        onSettingsClicked = {},
        onVerifyClicked = {},
        onCreateRoomClicked = {},
        onInvitesClicked = {},
        onRoomSettingsClicked = {},
        onMenuActionClicked = {},
    )
}

open class ScRoomListStateProvider : PreviewParameterProvider<RoomListState> {
    override val values: Sequence<RoomListState>
        get() = sequenceOf(
            aRoomListState(),
        )
}

internal fun aRoomListState() = RoomListState(
    matrixUser = MatrixUser(userId = UserId("@id:domain"), displayName = "Waltraute", avatarUrl = /*"example://uri"*/ "mxc://female"),
    showAvatarIndicator = false,
    roomList = AsyncData.Success(aRoomListRoomSummaryList()),
    filter = "filter",
    filteredRoomList = aRoomListRoomSummaryList(),
    hasNetworkConnection = true,
    snackbarMessage = null,
    displayVerificationPrompt = false,
    displayRecoveryKeyPrompt = false,
    invitesState = InvitesState.NoInvites,
    displaySearchResults = false,
    contextMenu = RoomListState.ContextMenu.Hidden,
    leaveRoomState = aLeaveRoomState(),
    displayMigrationStatus = false,
    eventSink = {},
    spacesList = aSpacesList(),
    spaceUnreadCounts = aSpacesUnreadCountMap(),
)

internal fun aRoomListRoomSummaryList(): ImmutableList<RoomListRoomSummary> {
    return persistentListOf(
        aRoomListRoomSummary(
            name = "Family Chat",
            timestamp = "14:16",
            lastMessage = senderSpanned("Bob", "Hum"),
            avatarData = AvatarData("!id", "gg", "mxc://family", size = AvatarSize.RoomListItem),
            numberOfUnreadNotifications = 1,
        ),
        aRoomListRoomSummary(
            name = "Der Ring",
            timestamp = "12:18",
            lastMessage = senderSpanned("Brünnhilde", "Hojotoho!"),
            avatarData = AvatarData("!id", "Ring", url="mxc://ring", size = AvatarSize.RoomListItem),
        ),
        aRoomListRoomSummary(
            name = "Wotan",
            timestamp = "Yesterday",
            lastMessage = "Hey Alter!",
            avatarData = AvatarData("!id", "Wotan", "mxc://wotan", size = AvatarSize.RoomListItem),
        ),
        aRoomListRoomSummary(
            name = "Die Walküren",
            timestamp = "Feb 22",
            lastMessage = senderSpanned("Waltraute", "Grimgerd' und Roßweiße!"),
            avatarData = AvatarData("!id", "gg", "mxc://valkyrie", size = AvatarSize.RoomListItem),
        ),
        aRoomListRoomSummary(
            name = "DIY mice",
            timestamp = "Oct 4",
            lastMessage = senderSpanned("Jens", "I like juice"),
            avatarData = AvatarData("!id", "ft", "mxc://electric", size = AvatarSize.RoomListItem),
        ),
        aRoomListRoomSummary(
            name = "Flying Turtles",
            timestamp = "Sep 27",
            lastMessage = "I have to find the whales",
            avatarData = AvatarData("!id", "ft", "mxc://flying", size = AvatarSize.RoomListItem),
        ),
        aRoomListRoomSummary(
            name = "Once upon a time",
            timestamp = "Sep 27",
            lastMessage = senderSpanned("Jack", "Arrr!"),
            avatarData = AvatarData("!id", "ft", "mxc://duck", size = AvatarSize.RoomListItem),
        ),
        aRoomListRoomSummary(
            name = "Bob",
            timestamp = "Sep 3",
            lastMessage = "I like turtles.",
            avatarData = AvatarData("!id", "ft", "mxc://bob", size = AvatarSize.RoomListItem),
        ),
    )
}

private fun aLeaveRoomState(
    confirmation: LeaveRoomState.Confirmation = LeaveRoomState.Confirmation.Hidden,
    progress: LeaveRoomState.Progress = LeaveRoomState.Progress.Hidden,
    error: LeaveRoomState.Error = LeaveRoomState.Error.Hidden,
) = LeaveRoomState(
    confirmation = confirmation,
    progress = progress,
    error = error,
    eventSink = {},
)

private fun senderSpanned(senderDisplayName: String, message: String): AnnotatedString {
    return buildAnnotatedString {
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
            append(senderDisplayName)
        }
        append(": ")
        append(message)
    }
}


internal fun aRoomListRoomSummary(
    id: String = "!roomId:domain",
    name: String = "Room name",
    numberOfUnreadMessages: Int = 0,
    numberOfUnreadMentions: Int = 0,
    numberOfUnreadNotifications: Int = 0,
    isMarkedUnread: Boolean = false,
    lastMessage: CharSequence? = "Last message",
    timestamp: String? = lastMessage?.let { "88:88" },
    isPlaceholder: Boolean = false,
    notificationMode: RoomNotificationMode? = null,
    hasRoomCall: Boolean = false,
    avatarData: AvatarData = AvatarData(id, name, size = AvatarSize.RoomListItem),
    isDm: Boolean = false,
) = RoomListRoomSummary(
    id = id,
    roomId = RoomId(id),
    name = name,
    numberOfUnreadMessages = numberOfUnreadMessages,
    numberOfUnreadMentions = numberOfUnreadMentions,
    numberOfUnreadNotifications = numberOfUnreadNotifications,
    highlightCount = numberOfUnreadMentions,
    notificationCount = numberOfUnreadNotifications,
    unreadCount = numberOfUnreadMessages,
    isMarkedUnread = isMarkedUnread,
    timestamp = timestamp,
    lastMessage = lastMessage,
    avatarData = avatarData,
    isPlaceholder = isPlaceholder,
    userDefinedNotificationMode = notificationMode,
    hasRoomCall = hasRoomCall,
    isDm = isDm,
)

internal fun aSpacesList() = persistentListOf(
    aSpace("friends", "A space for friends", "mxc://friends"),
    aSpace("cake", "Cake Community", "mxc://cake"),
    aSpace("matrix", "Matrix Community", "mxc://matrix"),
    aSpace("test", "Testspace", "mxc://test"),
)

internal fun aSpacesUnreadCountMap() = persistentMapOf(
    "friends" to SpaceUnreadCountsDataSource.SpaceUnreadCounts(notifiedChats = 1, notifiedMessages = 1),
    null to SpaceUnreadCountsDataSource.SpaceUnreadCounts(notifiedChats = 1, notifiedMessages = 1),
)

internal fun aSpace(id: String, name: String, avatar: String?) = SpaceListDataSource.SpaceHierarchyItem(
    info = aRoomListRoomSummary(
        id = id,
        name = name,
        avatarData = AvatarData(id, name, avatar, size = AvatarSize.RoomListItem),
    ),
    order = null,
    spaces = persistentListOf(),
    flattenedSpaces = persistentListOf(),
    flattenedRooms = persistentListOf(),
)

