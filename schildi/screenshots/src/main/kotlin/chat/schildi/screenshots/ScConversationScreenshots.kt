package chat.schildi.screenshots

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.messages.impl.MessagesState
import io.element.android.features.messages.impl.MessagesView
import io.element.android.features.messages.impl.aMessagesState
import io.element.android.features.messages.impl.messagecomposer.aMessageComposerState
import io.element.android.features.messages.impl.timeline.aPaginationState
import io.element.android.features.messages.impl.timeline.aTimelineItemDaySeparator
import io.element.android.features.messages.impl.timeline.aTimelineState
import io.element.android.features.messages.impl.timeline.model.AggregatedReaction
import io.element.android.features.messages.impl.timeline.model.InReplyToDetails
import io.element.android.features.messages.impl.timeline.model.ReadReceiptData
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.TimelineItemGroupPosition
import io.element.android.features.messages.impl.timeline.model.TimelineItemReactions
import io.element.android.features.messages.impl.timeline.model.TimelineItemReadReceipts
import io.element.android.features.messages.impl.timeline.model.anAggregatedReaction
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemImageContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextContent
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.TransactionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.api.timeline.item.TimelineItemDebugInfo
import io.element.android.libraries.matrix.api.timeline.item.event.LocalEventSendState
import io.element.android.libraries.matrix.ui.components.A_BLUR_HASH
import io.element.android.libraries.textcomposer.model.MessageComposerMode
import io.element.android.wysiwyg.compose.RichTextEditorState
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import java.util.UUID
import kotlin.random.Random

@ScScreenshot
@Composable
internal fun MessagesViewPreview(@PreviewParameter(ScScreenshotMessagesStateProvider::class) state: MessagesState) = ScScreenshot {
    MessagesView(
        state = state,
        onBackPressed = {},
        onRoomDetailsClicked = {},
        onEventClicked = { false },
        onPreviewAttachments = {},
        onUserDataClicked = {},
        onSendLocationClicked = {},
        onCreatePollClicked = {},
        onJoinCallClicked = {},
        forceJumpToBottomVisibility = false,
    )
}

open class ScScreenshotMessagesStateProvider : PreviewParameterProvider<MessagesState> {
    private val me = ScreenshotChatParticipant("Me", isMe = true)
    private val bob = ScreenshotChatParticipant("Bob", "mxc://bob")
    override val values: Sequence<MessagesState>
        get() = sequenceOf(
            aMessagesState(
                roomName = AsyncData.Success("Family Chat"),
                roomAvatar = AsyncData.Success(AvatarData("!", "Family Chat", url = "mxc://family", size = AvatarSize.RoomListItem)),
                timelineState = aTimelineState(
                    renderReadReceipts = true,
                    timelineItems = listOf(
                        aTimelineItemDaySeparator(),
                        aTimelineItemEvent(
                            user = me,
                            content = aTimelineItemTextContent("Now, what is this SchildiChat? Can somebody explain?"),
                            groupPosition = TimelineItemGroupPosition.None,
                            sentTime = "8:50",
                        ),
                        aTimelineItemEvent(
                            user = bob,
                            content = aTimelineItemImageContent("mxc://real", "It's the post turtle that I send my Matrix messages with ^", w=750, h=500),
                            groupPosition = TimelineItemGroupPosition.First,
                            sentTime = "12:14",
                            timelineItemReactions = aTimelineItemReactions(listOf(
                                anAggregatedReaction(key = "👀")
                            ))
                        ),
                        aTimelineItemEvent(
                            user = bob,
                            content = aTimelineItemTextContent("It's based on Element - you know, atoms and alike."),
                            groupPosition = TimelineItemGroupPosition.Middle,
                            sentTime = "12:15",
                        ),
                        aTimelineItemEvent(
                            user = bob,
                            content = aTimelineItemTextContent("Ah moment, it's based on Element X, the Matrix messenger. SchildiChat adds some nice features and UI changes to it 😉"),
                            groupPosition = TimelineItemGroupPosition.Last,
                            sentTime = "12:17",
                            /*
                            readReceiptState = aTimelineItemReadReceipts(listOf(
                                aReadReceipt("mxc://bob"),
                                aReadReceipt("mxc://female"),
                                aReadReceipt("mxc://male"),
                            ))
                             */
                        ),
                        aTimelineItemEvent(
                            user = me,
                            content = aTimelineItemTextContent("Oh wow, it can already filter by Matrix spaces! 🤯"),
                            groupPosition = TimelineItemGroupPosition.None,
                            sentTime = "12:22",
                        ),
                    ).reversed().toPersistentList(),
                    paginationState = aPaginationState(isBackPaginating = false, hasMoreToLoadBackwards = false, beginningOfRoomReached = false)
                ),
                composerState = aMessageComposerState(
                    richTextEditorState = RichTextEditorState("", initialFocus = true),
                    isFullScreen = false,
                    mode = MessageComposerMode.Normal,
                ),
            ),
        )
}


internal fun aTimelineItemEvent(
    user: ScreenshotChatParticipant,
    eventId: EventId = EventId("\$" + Random.nextInt().toString()),
    transactionId: TransactionId? = null,
    isEditable: Boolean = false,
    content: TimelineItemEventContent = aTimelineItemTextContent(),
    groupPosition: TimelineItemGroupPosition = TimelineItemGroupPosition.None,
    sendState: LocalEventSendState? = null,
    inReplyTo: InReplyToDetails? = null,
    isThreaded: Boolean = false,
    debugInfo: TimelineItemDebugInfo = aTimelineItemDebugInfo(),
    timelineItemReactions: TimelineItemReactions = aTimelineItemReactions(),
    readReceiptState: TimelineItemReadReceipts = aTimelineItemReadReceipts(),
    sentTime: String = "12:34",
): TimelineItem.Event {
    return TimelineItem.Event(
        id = UUID.randomUUID().toString(),
        eventId = eventId,
        transactionId = transactionId,
        senderId = UserId("@senderId:domain"),
        senderAvatar = AvatarData("@senderId:domain", user.displayName, url = user.avatarUrl, size = AvatarSize.TimelineSender),
        content = content,
        reactionsState = timelineItemReactions,
        readReceiptState = readReceiptState,
        sentTime = sentTime,
        isMine = user.isMe,
        isEditable = isEditable,
        senderDisplayName = user.displayName,
        groupPosition = groupPosition,
        localSendState = sendState,
        inReplyTo = inReplyTo,
        debugInfo = debugInfo,
        isThreaded = isThreaded,
        origin = null
    )
}

fun aTimelineItemTextContent(text: String = "Text") = TimelineItemTextContent(
    body = text,
    htmlDocument = null,
    formattedBody = null,
    isEdited = false,
)

fun aTimelineItemImageContent(url: String = "", caption: String? = null, w: Int? = null, h: Int? = null) = TimelineItemImageContent(
    body = caption ?: "body",
    filename = "filename".takeIf { caption != null },
    mediaSource = MediaSource(url),
    thumbnailSource = null,
    mimeType = "image/jpg",
    blurhash = A_BLUR_HASH,
    width = w,
    height = h,
    aspectRatio = if (w != null && h != null) w.toFloat()/h else null,
    formattedFileSize = "4MB",
    fileExtension = "jpg"
)


fun aTimelineItemReactions(
    reactions: List<AggregatedReaction> = emptyList(),
): TimelineItemReactions {
    return TimelineItemReactions(
        reactions = reactions.toPersistentList(),
    )
}

fun aReadReceipt(url: String? = null, name: String = "hmpf", id: String = "!", formattedDate: String = ""): ReadReceiptData {
    return ReadReceiptData(
        AvatarData(
            id = id,
            name = name,
            url = url,
            size = AvatarSize.TimelineReadReceipt,
        ),
        formattedDate,
    )
}

internal fun aTimelineItemDebugInfo(
    model: String = "Rust(Model())",
    originalJson: String? = null,
    latestEditedJson: String? = null,
) = TimelineItemDebugInfo(
    model,
    originalJson,
    latestEditedJson
)

internal fun aTimelineItemReadReceipts(
    receipts: List<ReadReceiptData> = emptyList(),
): TimelineItemReadReceipts {
    return TimelineItemReadReceipts(
        receipts = receipts.toImmutableList(),
    )
}

internal data class ScreenshotChatParticipant(
    val displayName: String,
    val avatarUrl: String? = null,
    val isMe: Boolean = false,
)
