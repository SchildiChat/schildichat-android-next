/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room

import io.element.android.libraries.matrix.api.core.DeviceId
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.ProgressCallback
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SendHandle
import io.element.android.libraries.matrix.api.core.TransactionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.encryption.identity.IdentityStateChange
import io.element.android.libraries.matrix.api.media.AudioInfo
import io.element.android.libraries.matrix.api.media.FileInfo
import io.element.android.libraries.matrix.api.media.ImageInfo
import io.element.android.libraries.matrix.api.media.MediaUploadHandler
import io.element.android.libraries.matrix.api.media.VideoInfo
import io.element.android.libraries.matrix.api.poll.PollKind
import io.element.android.libraries.matrix.api.room.history.RoomHistoryVisibility
import io.element.android.libraries.matrix.api.room.join.JoinRule
import io.element.android.libraries.matrix.api.room.knock.KnockRequest
import io.element.android.libraries.matrix.api.room.location.AssetType
import io.element.android.libraries.matrix.api.room.message.ReplyParameters
import io.element.android.libraries.matrix.api.room.powerlevels.RoomPowerLevels
import io.element.android.libraries.matrix.api.room.powerlevels.UserRoleChange
import io.element.android.libraries.matrix.api.roomdirectory.RoomVisibility
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.api.timeline.item.event.EventOrTransactionId
import io.element.android.libraries.matrix.api.widget.MatrixWidgetDriver
import io.element.android.libraries.matrix.api.widget.MatrixWidgetSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

interface JoinedRoom : BaseRoom {
    val syncUpdateFlow: StateFlow<Long>

    val roomTypingMembersFlow: Flow<List<UserId>>
    val identityStateChangesFlow: Flow<List<IdentityStateChange>>
    val roomNotificationSettingsStateFlow: StateFlow<RoomNotificationSettingsState>

    /**
     * The current knock requests in the room as a Flow.
     */
    val knockRequestsFlow: Flow<List<KnockRequest>>

    /**
     * A one-to-one is a room with exactly 2 members.
     * See [the Matrix spec](https://spec.matrix.org/latest/client-server-api/#default-underride-rules).
     */
    val isOneToOne: Boolean get() = info().activeMembersCount == 2L

    /**
     * The live timeline of the room. Must be used to send Event to a room.
     */
    val liveTimeline: Timeline

    /**
     * Create a new timeline.
     * @param createTimelineParams contains parameters about how to filter the timeline. Will also configure the date separators.
     */
    suspend fun createTimeline(
        createTimelineParams: CreateTimelineParams,
    ): Result<Timeline>

    suspend fun sendMessage(body: String, htmlBody: String?, intentionalMentions: List<IntentionalMention>): Result<Unit>

    suspend fun editMessage(eventId: EventId, body: String, htmlBody: String?, intentionalMentions: List<IntentionalMention>): Result<Unit>

    suspend fun sendImage(
        file: File,
        thumbnailFile: File?,
        imageInfo: ImageInfo,
        caption: String?,
        formattedCaption: String?,
        progressCallback: ProgressCallback?,
        replyParameters: ReplyParameters?,
    ): Result<MediaUploadHandler>

    suspend fun sendVideo(
        file: File,
        thumbnailFile: File?,
        videoInfo: VideoInfo,
        caption: String?,
        formattedCaption: String?,
        progressCallback: ProgressCallback?,
        replyParameters: ReplyParameters?,
    ): Result<MediaUploadHandler>

    suspend fun sendAudio(
        file: File,
        audioInfo: AudioInfo,
        caption: String?,
        formattedCaption: String?,
        progressCallback: ProgressCallback?,
        replyParameters: ReplyParameters?,
    ): Result<MediaUploadHandler>

    suspend fun sendFile(
        file: File,
        fileInfo: FileInfo,
        caption: String?,
        formattedCaption: String?,
        progressCallback: ProgressCallback?,
        replyParameters: ReplyParameters?,
    ): Result<MediaUploadHandler>

    suspend fun sendVoiceMessage(
        file: File,
        audioInfo: AudioInfo,
        waveform: List<Float>,
        progressCallback: ProgressCallback?,
        replyParameters: ReplyParameters?,
    ): Result<MediaUploadHandler>

    /**
     * Share a location message in the room.
     *
     * @param body A human readable textual representation of the location.
     * @param geoUri A geo URI (RFC 5870) representing the location e.g. `geo:51.5008,0.1247;u=35`.
     *  Respectively: latitude, longitude, and (optional) uncertainty.
     * @param description Optional description of the location to display to the user.
     * @param zoomLevel Optional zoom level to display the map at.
     * @param assetType Optional type of the location asset.
     *  Set to SENDER if sharing own location. Set to PIN if sharing any location.
     */
    suspend fun sendLocation(
        body: String,
        geoUri: String,
        description: String? = null,
        zoomLevel: Int? = null,
        assetType: AssetType? = null,
    ): Result<Unit>

    /**
     * Create a poll in the room.
     *
     * @param question The question to ask.
     * @param answers The list of answers.
     * @param maxSelections The maximum number of answers that can be selected.
     * @param pollKind The kind of poll to create.
     */
    suspend fun createPoll(
        question: String,
        answers: List<String>,
        maxSelections: Int,
        pollKind: PollKind,
    ): Result<Unit>

    /**
     * Edit a poll in the room.
     *
     * @param pollStartId The event ID of the poll start event.
     * @param question The question to ask.
     * @param answers The list of answers.
     * @param maxSelections The maximum number of answers that can be selected.
     * @param pollKind The kind of poll to create.
     */
    suspend fun editPoll(
        pollStartId: EventId,
        question: String,
        answers: List<String>,
        maxSelections: Int,
        pollKind: PollKind,
    ): Result<Unit>

    /**
     * Send a response to a poll.
     *
     * @param pollStartId The event ID of the poll start event.
     * @param answers The list of answer ids to send.
     */
    suspend fun sendPollResponse(pollStartId: EventId, answers: List<String>): Result<Unit>

    /**
     * Ends a poll in the room.
     *
     * @param pollStartId The event ID of the poll start event.
     * @param text Fallback text of the poll end event.
     */
    suspend fun endPoll(pollStartId: EventId, text: String): Result<Unit>

    /**
     * Send a typing notification.
     * @param isTyping True if the user is typing, false otherwise.
     */
    suspend fun typingNotice(isTyping: Boolean): Result<Unit>

    suspend fun toggleReaction(emoji: String, eventOrTransactionId: EventOrTransactionId): Result<Unit>

    suspend fun forwardEvent(eventId: EventId, roomIds: List<RoomId>): Result<Unit>

    suspend fun cancelSend(transactionId: TransactionId): Result<Unit>

    suspend fun inviteUserById(id: UserId): Result<Unit>

    suspend fun updateAvatar(mimeType: String, data: ByteArray): Result<Unit>

    suspend fun removeAvatar(): Result<Unit>

    suspend fun updateRoomNotificationSettings(): Result<Unit>

    /**
     * Update the canonical alias of the room.
     *
     * Note that publishing the alias in the room directory is done separately.
     */
    suspend fun updateCanonicalAlias(
        canonicalAlias: RoomAlias?,
        alternativeAliases: List<RoomAlias>
    ): Result<Unit>

    /**
     * Update the room's visibility in the room directory.
     */
    suspend fun updateRoomVisibility(roomVisibility: RoomVisibility): Result<Unit>

    /**
     * Update room history visibility for this room.
     */
    suspend fun updateHistoryVisibility(historyVisibility: RoomHistoryVisibility): Result<Unit>

    /**
     * Publish a new room alias for this room in the room directory.
     *
     * Returns:
     * - `true` if the room alias didn't exist and it's now published.
     * - `false` if the room alias was already present so it couldn't be
     * published.
     */
    suspend fun publishRoomAliasInRoomDirectory(roomAlias: RoomAlias): Result<Boolean>

    /**
     * Remove an existing room alias for this room in the room directory.
     *
     * Returns:
     * - `true` if the room alias was present and it's now removed from the
     * room directory.
     * - `false` if the room alias didn't exist so it couldn't be removed.
     */
    suspend fun removeRoomAliasFromRoomDirectory(roomAlias: RoomAlias): Result<Boolean>

    /**
     * Enable End-to-end encryption in this room.
     */
    suspend fun enableEncryption(): Result<Unit>

    /**
     * Update the join rule for this room.
     */
    suspend fun updateJoinRule(joinRule: JoinRule): Result<Unit>

    suspend fun updateUsersRoles(changes: List<UserRoleChange>): Result<Unit>

    suspend fun updatePowerLevels(roomPowerLevels: RoomPowerLevels): Result<Unit>

    suspend fun resetPowerLevels(): Result<RoomPowerLevels>

    suspend fun setName(name: String): Result<Unit>

    suspend fun setTopic(topic: String): Result<Unit>

    suspend fun reportContent(eventId: EventId, reason: String, blockUserId: UserId?): Result<Unit>

    suspend fun kickUser(userId: UserId, reason: String? = null): Result<Unit>

    suspend fun banUser(userId: UserId, reason: String? = null): Result<Unit>

    suspend fun unbanUser(userId: UserId, reason: String? = null): Result<Unit>

    /**
     * Generates a Widget url to display in a [android.webkit.WebView] given the provided parameters.
     * @param widgetSettings The widget settings to use.
     * @param clientId The client id to use. It should be unique per app install.
     * @param languageTag The language tag to use. If null, the default language will be used.
     * @param theme The theme to use. If null, the default theme will be used.
     * @return The resulting url, or a failure.
     */
    suspend fun generateWidgetWebViewUrl(
        widgetSettings: MatrixWidgetSettings,
        clientId: String,
        languageTag: String?,
        theme: String?,
    ): Result<String>

    /**
     * Get a [MatrixWidgetDriver] for the provided [widgetSettings].
     * @param widgetSettings The widget settings to use.
     * @return The resulting [MatrixWidgetDriver], or a failure.
     */
    fun getWidgetDriver(widgetSettings: MatrixWidgetSettings): Result<MatrixWidgetDriver>

    /**
     * Send an Element Call started notification if needed.
     */
    suspend fun sendCallNotificationIfNeeded(): Result<Unit>

    suspend fun setSendQueueEnabled(enabled: Boolean)

    /**
     * Ignore the local trust for the given devices and resend messages that failed to send because said devices are unverified.
     *
     * @param devices The map of users identifiers to device identifiers received in the error
     * @param sendHandle The send queue handle of the local echo the send error applies to. It can be used to retry the upload.
     *
     */
    suspend fun ignoreDeviceTrustAndResend(devices: Map<UserId, List<DeviceId>>, sendHandle: SendHandle): Result<Unit>

    /**
     * Remove verification requirements for the given users and
     * resend messages that failed to send because their identities were no longer verified.
     *
     * @param userIds The list of users identifiers received in the error.
     * @param sendHandle The send queue handle of the local echo the send error applies to. It can be used to retry the upload.
     *
     */
    suspend fun withdrawVerificationAndResend(userIds: List<UserId>, sendHandle: SendHandle): Result<Unit>
}
