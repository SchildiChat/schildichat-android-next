/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl

import chat.schildi.lib.preferences.ScPreferencesStore
import io.element.android.libraries.androidutils.file.getSizeOfFiles
import io.element.android.libraries.androidutils.file.safeDelete
import io.element.android.libraries.core.bool.orFalse
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.coroutine.childScope
import io.element.android.libraries.core.data.tryOrNull
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.DeviceId
import io.element.android.libraries.matrix.api.core.ProgressCallback
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import io.element.android.libraries.matrix.api.createroom.CreateRoomParameters
import io.element.android.libraries.matrix.api.createroom.RoomPreset
import io.element.android.libraries.matrix.api.encryption.EncryptionService
import io.element.android.libraries.matrix.api.media.MatrixMediaLoader
import io.element.android.libraries.matrix.api.notification.NotificationService
import io.element.android.libraries.matrix.api.notificationsettings.NotificationSettingsService
import io.element.android.libraries.matrix.api.oidc.AccountManagementAction
import io.element.android.libraries.matrix.api.pusher.PushersService
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.PendingRoom
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomMembershipObserver
import io.element.android.libraries.matrix.api.room.alias.ResolvedRoomAlias
import io.element.android.libraries.matrix.api.room.join.JoinRule
import io.element.android.libraries.matrix.api.room.preview.RoomPreviewInfo
import io.element.android.libraries.matrix.api.roomdirectory.RoomDirectoryService
import io.element.android.libraries.matrix.api.roomdirectory.RoomVisibility
import io.element.android.libraries.matrix.api.roomlist.RoomListService
import io.element.android.libraries.matrix.api.roomlist.RoomSummary
import io.element.android.libraries.matrix.api.sync.SlidingSyncVersion
import io.element.android.libraries.matrix.api.sync.SyncService
import io.element.android.libraries.matrix.api.sync.SyncState
import io.element.android.libraries.matrix.api.user.MatrixSearchUserResults
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import io.element.android.libraries.matrix.impl.core.toProgressWatcher
import io.element.android.libraries.matrix.impl.encryption.RustEncryptionService
import io.element.android.libraries.matrix.impl.media.RustMediaLoader
import io.element.android.libraries.matrix.impl.notification.RustNotificationService
import io.element.android.libraries.matrix.impl.notificationsettings.RustNotificationSettingsService
import io.element.android.libraries.matrix.impl.oidc.toRustAction
import io.element.android.libraries.matrix.impl.pushers.RustPushersService
import io.element.android.libraries.matrix.impl.room.RoomContentForwarder
import io.element.android.libraries.matrix.impl.room.RoomSyncSubscriber
import io.element.android.libraries.matrix.impl.room.RustRoomFactory
import io.element.android.libraries.matrix.impl.room.TimelineEventTypeFilterFactory
import io.element.android.libraries.matrix.impl.room.join.map
import io.element.android.libraries.matrix.impl.room.preview.RoomPreviewInfoMapper
import io.element.android.libraries.matrix.impl.roomdirectory.RustRoomDirectoryService
import io.element.android.libraries.matrix.impl.roomdirectory.map
import io.element.android.libraries.matrix.impl.roomlist.RoomListFactory
import io.element.android.libraries.matrix.impl.roomlist.RustRoomListService
import io.element.android.libraries.matrix.impl.sync.RustSyncService
import io.element.android.libraries.matrix.impl.sync.map
import io.element.android.libraries.matrix.impl.usersearch.UserProfileMapper
import io.element.android.libraries.matrix.impl.usersearch.UserSearchResultMapper
import io.element.android.libraries.matrix.impl.util.SessionPathsProvider
import io.element.android.libraries.matrix.impl.util.cancelAndDestroy
import io.element.android.libraries.matrix.impl.util.mxCallbackFlow
import io.element.android.libraries.matrix.impl.verification.RustSessionVerificationService
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.services.toolbox.api.systemclock.SystemClock
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.matrix.rustcomponents.sdk.AuthData
import org.matrix.rustcomponents.sdk.AuthDataPasswordDetails
import org.matrix.rustcomponents.sdk.Client
import org.matrix.rustcomponents.sdk.ClientException
import org.matrix.rustcomponents.sdk.IgnoredUsersListener
import org.matrix.rustcomponents.sdk.NotificationProcessSetup
import org.matrix.rustcomponents.sdk.PowerLevels
import org.matrix.rustcomponents.sdk.SendQueueRoomErrorListener
import org.matrix.rustcomponents.sdk.TaskHandle
import org.matrix.rustcomponents.sdk.use
import timber.log.Timber
import java.io.File
import java.util.Optional
import kotlin.jvm.optionals.getOrNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import org.matrix.rustcomponents.sdk.CreateRoomParameters as RustCreateRoomParameters
import org.matrix.rustcomponents.sdk.RoomPreset as RustRoomPreset
import org.matrix.rustcomponents.sdk.SyncService as ClientSyncService

class RustMatrixClient(
    private val innerClient: Client,
    private val baseDirectory: File,
    private val sessionStore: SessionStore,
    private val appCoroutineScope: CoroutineScope,
    private val sessionDelegate: RustClientSessionDelegate,
    private val scPreferencesStore: ScPreferencesStore,
    innerSyncService: ClientSyncService,
    dispatchers: CoroutineDispatchers,
    baseCacheDirectory: File,
    clock: SystemClock,
    timelineEventTypeFilterFactory: TimelineEventTypeFilterFactory,
    featureFlagService: FeatureFlagService,
) : MatrixClient {
    override val sessionId: UserId = UserId(innerClient.userId())
    override val deviceId: DeviceId = DeviceId(innerClient.deviceId())
    override val sessionCoroutineScope = appCoroutineScope.childScope(dispatchers.main, "Session-$sessionId")
    private val sessionDispatcher = dispatchers.io.limitedParallelism(64)

    private val innerRoomListService = innerSyncService.roomListService()

    private val rustSyncService = RustSyncService(
        inner = innerSyncService,
        dispatcher = sessionDispatcher,
        sessionCoroutineScope = sessionCoroutineScope
    )
    private val pushersService = RustPushersService(
        client = innerClient,
        dispatchers = dispatchers,
    )
    private val notificationProcessSetup = NotificationProcessSetup.SingleProcess(innerSyncService)
    private val innerNotificationClient = runBlocking { innerClient.notificationClient(notificationProcessSetup) }
    private val notificationService = RustNotificationService(innerNotificationClient, dispatchers, clock)
    private val notificationSettingsService = RustNotificationSettingsService(innerClient, dispatchers)
        .apply { start() }
    private val encryptionService = RustEncryptionService(
        client = innerClient,
        syncService = rustSyncService,
        sessionCoroutineScope = sessionCoroutineScope,
        dispatchers = dispatchers,
    )

    private val roomDirectoryService = RustRoomDirectoryService(
        client = innerClient,
        sessionDispatcher = sessionDispatcher,
    )

    private val sessionPathsProvider = SessionPathsProvider(sessionStore)

    private val roomSyncSubscriber: RoomSyncSubscriber = RoomSyncSubscriber(innerRoomListService, dispatchers)

    override val roomListService: RoomListService = RustRoomListService(
        innerRoomListService = innerRoomListService,
        sessionCoroutineScope = sessionCoroutineScope,
        sessionDispatcher = sessionDispatcher,
        roomListFactory = RoomListFactory(
            innerRoomListService = innerRoomListService,
            sessionCoroutineScope = sessionCoroutineScope,
        ),
        roomSyncSubscriber = roomSyncSubscriber,
        scPreferencesStore = scPreferencesStore,
    )

    private val verificationService = RustSessionVerificationService(
        client = innerClient,
        isSyncServiceReady = rustSyncService.syncState.map { it == SyncState.Running },
        sessionCoroutineScope = sessionCoroutineScope,
    )

    private val roomMembershipObserver = RoomMembershipObserver()
    private val roomFactory = RustRoomFactory(
        roomListService = roomListService,
        innerRoomListService = innerRoomListService,
        sessionId = sessionId,
        deviceId = deviceId,
        notificationSettingsService = notificationSettingsService,
        sessionCoroutineScope = sessionCoroutineScope,
        dispatchers = dispatchers,
        systemClock = clock,
        roomContentForwarder = RoomContentForwarder(innerRoomListService),
        roomSyncSubscriber = roomSyncSubscriber,
        timelineEventTypeFilterFactory = timelineEventTypeFilterFactory,
        featureFlagService = featureFlagService,
        roomMembershipObserver = roomMembershipObserver,
    )

    override val mediaLoader: MatrixMediaLoader = RustMediaLoader(
        baseCacheDirectory = baseCacheDirectory,
        dispatchers = dispatchers,
        innerClient = innerClient,
    )

    private var clientDelegateTaskHandle: TaskHandle? = innerClient.setDelegate(sessionDelegate)

    private val _userProfile: MutableStateFlow<MatrixUser> = MutableStateFlow(
        MatrixUser(
            userId = sessionId,
            // TODO cache for displayName?
            displayName = null,
            avatarUrl = innerClient.cachedAvatarUrl(),
        )
    )

    override val userProfile: StateFlow<MatrixUser> = _userProfile

    override val ignoredUsersFlow = mxCallbackFlow<ImmutableList<UserId>> {
        innerClient.subscribeToIgnoredUsers(object : IgnoredUsersListener {
            override fun call(ignoredUserIds: List<String>) {
                channel.trySend(ignoredUserIds.map(::UserId).toPersistentList())
            }
        })
    }
        .buffer(Channel.UNLIMITED)
        .stateIn(sessionCoroutineScope, started = SharingStarted.Eagerly, initialValue = persistentListOf())

    init {
        // Make sure the session delegate has a reference to the client to be able to logout on auth error
        sessionDelegate.bindClient(this)

        sessionCoroutineScope.launch {
            // Force a refresh of the profile
            getUserProfile()
        }
    }

    override fun userIdServerName(): String {
        return runCatching {
            innerClient.userIdServerName()
        }
            .onFailure {
                Timber.w(it, "Failed to get userIdServerName")
            }
            .getOrNull()
            ?: sessionId.value.substringAfter(":")
    }

    override suspend fun getUrl(url: String): Result<String> = withContext(sessionDispatcher) {
        runCatching {
            innerClient.getUrl(url)
        }
    }

    override suspend fun getRoom(roomId: RoomId): MatrixRoom? {
        return roomFactory.create(roomId)
    }

    override suspend fun getPendingRoom(roomId: RoomId): PendingRoom? {
        return roomFactory.createPendingRoom(roomId)
    }

    /**
     * Wait for the room to be available in the room list with the correct membership for the current user.
     * @param roomIdOrAlias the room id or alias to wait for
     * @param timeout the timeout to wait for the room to be available
     * @param currentUserMembership the membership to wait for
     * @throws TimeoutCancellationException if the room is not available after the timeout
     */
    private suspend fun awaitRoom(
        roomIdOrAlias: RoomIdOrAlias,
        timeout: Duration,
        currentUserMembership: CurrentUserMembership,
    ): RoomSummary {
        return withTimeout(timeout) {
            getRoomSummaryFlow(roomIdOrAlias)
                .mapNotNull { optionalRoomSummary -> optionalRoomSummary.getOrNull() }
                .filter { roomSummary -> roomSummary.info.currentUserMembership == currentUserMembership }
                .first()
                // Ensure that the room is ready
                .also { innerClient.awaitRoomRemoteEcho(it.roomId.value) }
        }
    }

    // SC additions
    override suspend fun getAccountData(eventType: String): String? = withContext(sessionDispatcher) {
        runCatching {
            innerClient.accountData(eventType)
        }.getOrNull()
    }
    override suspend fun getRoomAccountData(roomId: RoomId, eventType: String): String? = withContext(sessionDispatcher) {
        runCatching {
            innerClient.roomAccountData(roomId.value, eventType)
        }.getOrNull()
    }
    override suspend fun setAccountData(eventType: String, content: String) = withContext(sessionDispatcher) {
        runCatching {
            innerClient.setAccountData(eventType, content)
        }
        Unit
    }

    override suspend fun getUrlPreviewJson(url: String): String = withContext(sessionDispatcher) {
        innerClient.getUrlPreviewJson(url)
    }
    // SC additions end

    override suspend fun findDM(userId: UserId): RoomId? = withContext(sessionDispatcher) {
        innerClient.getDmRoom(userId.value)?.use { RoomId(it.id()) }
    }

    override suspend fun ignoreUser(userId: UserId): Result<Unit> = withContext(sessionDispatcher) {
        runCatching {
            innerClient.ignoreUser(userId.value)
        }
    }

    override suspend fun unignoreUser(userId: UserId): Result<Unit> = withContext(sessionDispatcher) {
        runCatching {
            innerClient.unignoreUser(userId.value)
        }
    }

    override suspend fun createRoom(createRoomParams: CreateRoomParameters): Result<RoomId> = withContext(sessionDispatcher) {
        runCatching {
            val rustParams = RustCreateRoomParameters(
                name = createRoomParams.name,
                topic = createRoomParams.topic,
                isEncrypted = createRoomParams.isEncrypted,
                isDirect = createRoomParams.isDirect,
                visibility = createRoomParams.visibility.map(),
                preset = when (createRoomParams.preset) {
                    RoomPreset.PRIVATE_CHAT -> RustRoomPreset.PRIVATE_CHAT
                    RoomPreset.TRUSTED_PRIVATE_CHAT -> RustRoomPreset.TRUSTED_PRIVATE_CHAT
                    RoomPreset.PUBLIC_CHAT -> RustRoomPreset.PUBLIC_CHAT
                },
                invite = createRoomParams.invite?.map { it.value },
                avatar = createRoomParams.avatar,
                powerLevelContentOverride = defaultRoomCreationPowerLevels.copy(
                    invite = if (createRoomParams.joinRuleOverride == JoinRule.Knock) {
                        // override the invite power level so it's the same as kick.
                        RoomMember.Role.MODERATOR.powerLevel.toInt()
                    } else {
                        null
                    }
                ),
                joinRuleOverride = createRoomParams.joinRuleOverride?.map(),
                canonicalAlias = createRoomParams.roomAliasName.getOrNull(),
            )
            val roomId = RoomId(innerClient.createRoom(rustParams))
            // Wait to receive the room back from the sync but do not returns failure if it fails.
            try {
                awaitRoom(roomId.toRoomIdOrAlias(), 30.seconds, CurrentUserMembership.JOINED)
            } catch (e: Exception) {
                Timber.e(e, "Timeout waiting for the room to be available in the room list")
            }
            roomId
        }
    }

    override suspend fun createDM(userId: UserId): Result<RoomId> {
        val createRoomParams = CreateRoomParameters(
            name = null,
            isEncrypted = true,
            isDirect = true,
            visibility = RoomVisibility.Private,
            preset = RoomPreset.TRUSTED_PRIVATE_CHAT,
            invite = listOf(userId),
        )
        return createRoom(createRoomParams)
    }

    override suspend fun getProfile(userId: UserId): Result<MatrixUser> = withContext(sessionDispatcher) {
        runCatching {
            innerClient.getProfile(userId.value).let(UserProfileMapper::map)
        }
    }

    override suspend fun getUserProfile(): Result<MatrixUser> = getProfile(sessionId)
        .onSuccess { _userProfile.tryEmit(it) }

    override suspend fun searchUsers(searchTerm: String, limit: Long): Result<MatrixSearchUserResults> =
        withContext(sessionDispatcher) {
            runCatching {
                innerClient.searchUsers(searchTerm, limit.toULong()).let(UserSearchResultMapper::map)
            }
        }

    override suspend fun setDisplayName(displayName: String): Result<Unit> =
        withContext(sessionDispatcher) {
            runCatching { innerClient.setDisplayName(displayName) }
        }

    override suspend fun uploadAvatar(mimeType: String, data: ByteArray): Result<Unit> =
        withContext(sessionDispatcher) {
            runCatching { innerClient.uploadAvatar(mimeType, data) }
        }

    override suspend fun removeAvatar(): Result<Unit> =
        withContext(sessionDispatcher) {
            runCatching { innerClient.removeAvatar() }
        }

    override suspend fun joinRoom(roomId: RoomId): Result<RoomSummary?> = withContext(sessionDispatcher) {
        runCatching {
            innerClient.joinRoomById(roomId.value).destroy()
            try {
                awaitRoom(roomId.toRoomIdOrAlias(), 10.seconds, CurrentUserMembership.JOINED)
            } catch (e: Exception) {
                Timber.e(e, "Timeout waiting for the room to be available in the room list")
                null
            }
        }
    }

    override suspend fun joinRoomByIdOrAlias(roomIdOrAlias: RoomIdOrAlias, serverNames: List<String>): Result<RoomSummary?> = withContext(sessionDispatcher) {
        runCatching {
            innerClient.joinRoomByIdOrAlias(
                roomIdOrAlias = roomIdOrAlias.identifier,
                serverNames = serverNames,
            ).destroy()
            try {
                awaitRoom(roomIdOrAlias, 10.seconds, CurrentUserMembership.JOINED)
            } catch (e: Exception) {
                Timber.e(e, "Timeout waiting for the room to be available in the room list")
                null
            }
        }
    }

    override suspend fun knockRoom(roomIdOrAlias: RoomIdOrAlias, message: String, serverNames: List<String>): Result<RoomSummary?> = withContext(
        sessionDispatcher
    ) {
        runCatching {
            innerClient.knock(roomIdOrAlias.identifier, message, serverNames).destroy()
            try {
                awaitRoom(roomIdOrAlias, 10.seconds, CurrentUserMembership.KNOCKED)
            } catch (e: Exception) {
                Timber.e(e, "Timeout waiting for the room to be available in the room list")
                null
            }
        }
    }

    override suspend fun trackRecentlyVisitedRoom(roomId: RoomId): Result<Unit> = withContext(sessionDispatcher) {
        runCatching {
            innerClient.trackRecentlyVisitedRoom(roomId.value)
        }
    }

    override suspend fun getRecentlyVisitedRooms(): Result<List<RoomId>> = withContext(sessionDispatcher) {
        runCatching {
            innerClient.getRecentlyVisitedRooms().map(::RoomId)
        }
    }

    override suspend fun resolveRoomAlias(roomAlias: RoomAlias): Result<Optional<ResolvedRoomAlias>> = withContext(sessionDispatcher) {
        runCatching {
            val result = innerClient.resolveRoomAlias(roomAlias.value)?.let {
                ResolvedRoomAlias(
                    roomId = RoomId(it.roomId),
                    servers = it.servers,
                )
            }
            Optional.ofNullable(result)
        }
    }

    override suspend fun getRoomPreviewInfo(roomIdOrAlias: RoomIdOrAlias, serverNames: List<String>): Result<RoomPreviewInfo> = withContext(sessionDispatcher) {
        runCatching {
            when (roomIdOrAlias) {
                is RoomIdOrAlias.Alias -> innerClient.getRoomPreviewFromRoomAlias(roomIdOrAlias.roomAlias.value)
                is RoomIdOrAlias.Id -> innerClient.getRoomPreviewFromRoomId(roomIdOrAlias.roomId.value, serverNames)
            }.use { roomPreview ->
                RoomPreviewInfoMapper.map(roomPreview.info())
            }
        }
    }

    override fun syncService(): SyncService = rustSyncService

    override fun sessionVerificationService(): SessionVerificationService = verificationService

    override fun pushersService(): PushersService = pushersService

    override fun notificationService(): NotificationService = notificationService

    override fun encryptionService(): EncryptionService = encryptionService

    override fun notificationSettingsService(): NotificationSettingsService = notificationSettingsService

    override fun roomDirectoryService(): RoomDirectoryService = roomDirectoryService

    override fun close() {
        appCoroutineScope.launch {
            roomFactory.destroy()
            rustSyncService.destroy()
        }
        sessionCoroutineScope.cancel()
        clientDelegateTaskHandle?.cancelAndDestroy()
        notificationSettingsService.destroy()
        verificationService.destroy()
    }

    override suspend fun getCacheSize(): Long {
        return baseDirectory.getCacheSize()
    }

    override suspend fun clearCache() {
        close()
        deleteSessionDirectory(deleteCryptoDb = false)
    }

    override suspend fun logout(userInitiated: Boolean, ignoreSdkError: Boolean): String? {
        var result: String? = null
        // Remove current delegate so we don't receive an auth error
        clientDelegateTaskHandle?.cancelAndDestroy()
        clientDelegateTaskHandle = null
        withContext(sessionDispatcher) {
            if (userInitiated) {
                try {
                    result = innerClient.logout()
                } catch (failure: Throwable) {
                    if (ignoreSdkError) {
                        Timber.e(failure, "Fail to call logout on HS. Still delete local files.")
                    } else {
                        // If the logout failed we need to restore the delegate
                        clientDelegateTaskHandle = innerClient.setDelegate(sessionDelegate)
                        Timber.e(failure, "Fail to call logout on HS.")
                        throw failure
                    }
                }
            }
            close()

            deleteSessionDirectory(deleteCryptoDb = true)
            if (userInitiated) {
                sessionStore.removeSession(sessionId.value)
            }
        }
        return result
    }

    override fun canDeactivateAccount(): Boolean {
        return runCatching {
            innerClient.canDeactivateAccount()
        }
            .getOrNull()
            .orFalse()
    }

    override suspend fun deactivateAccount(password: String, eraseData: Boolean): Result<Unit> = withContext(sessionDispatcher) {
        Timber.w("Deactivating account")
        // Remove current delegate so we don't receive an auth error
        clientDelegateTaskHandle?.cancelAndDestroy()
        clientDelegateTaskHandle = null
        runCatching {
            // First call without AuthData, should fail
            val firstAttempt = runCatching {
                innerClient.deactivateAccount(
                    authData = null,
                    eraseData = eraseData,
                )
            }
            if (firstAttempt.isFailure) {
                Timber.w(firstAttempt.exceptionOrNull(), "Expected failure, try again")
                // This is expected, try again with the password
                runCatching {
                    innerClient.deactivateAccount(
                        authData = AuthData.Password(
                            passwordDetails = AuthDataPasswordDetails(
                                identifier = sessionId.value,
                                password = password,
                            ),
                        ),
                        eraseData = eraseData,
                    )
                }.onFailure {
                    Timber.e(it, "Failed to deactivate account")
                    // If the deactivation failed we need to restore the delegate
                    clientDelegateTaskHandle = innerClient.setDelegate(sessionDelegate)
                    throw it
                }
            }
            close()
            deleteSessionDirectory(deleteCryptoDb = true)
            sessionStore.removeSession(sessionId.value)
        }.onFailure {
            Timber.e(it, "Failed to deactivate account")
        }
    }

    override suspend fun getAccountManagementUrl(action: AccountManagementAction?): Result<String?> = withContext(sessionDispatcher) {
        val rustAction = action?.toRustAction()
        runCatching {
            innerClient.accountUrl(rustAction)
        }
    }

    override suspend fun uploadMedia(mimeType: String, data: ByteArray, progressCallback: ProgressCallback?): Result<String> = withContext(sessionDispatcher) {
        runCatching {
            innerClient.uploadMedia(mimeType, data, progressCallback?.toProgressWatcher())
        }
    }

    override fun roomMembershipObserver(): RoomMembershipObserver = roomMembershipObserver

    override fun getRoomSummaryFlow(roomIdOrAlias: RoomIdOrAlias): Flow<Optional<RoomSummary>> {
        val predicate: (RoomSummary) -> Boolean = when (roomIdOrAlias) {
            is RoomIdOrAlias.Alias -> { roomSummary ->
                roomSummary.info.aliases.contains(roomIdOrAlias.roomAlias)
            }
            is RoomIdOrAlias.Id -> { roomSummary ->
                roomSummary.roomId == roomIdOrAlias.roomId
            }
        }
        return roomListService.allRooms.summaries
            .map { roomSummaries ->
                val roomSummary = roomSummaries.firstOrNull(predicate)
                Optional.ofNullable(roomSummary)
            }
            .distinctUntilChanged()
    }

    override suspend fun setAllSendQueuesEnabled(enabled: Boolean) {
        withContext(sessionDispatcher) {
            Timber.i("setAllSendQueuesEnabled($enabled)")
            tryOrNull {
                innerClient.enableAllSendQueues(enabled)
            }
        }
    }

    override fun sendQueueDisabledFlow(): Flow<RoomId> = mxCallbackFlow {
        innerClient.subscribeToSendQueueStatus(object : SendQueueRoomErrorListener {
            override fun onError(roomId: String, error: ClientException) {
                trySend(RoomId(roomId))
            }
        })
    }.buffer(Channel.UNLIMITED)

    override suspend fun availableSlidingSyncVersions(): Result<List<SlidingSyncVersion>> = withContext(sessionDispatcher) {
        runCatching {
            innerClient.availableSlidingSyncVersions().map { it.map() }
        }
    }

    override suspend fun currentSlidingSyncVersion(): Result<SlidingSyncVersion> = withContext(sessionDispatcher) {
        runCatching {
            innerClient.session().slidingSyncVersion.map()
        }
    }

    private suspend fun File.getCacheSize(
        includeCryptoDb: Boolean = false,
    ): Long = withContext(sessionDispatcher) {
        val sessionDirectory = sessionPathsProvider.provides(sessionId) ?: return@withContext 0L
        val cacheSize = sessionDirectory.cacheDirectory.getSizeOfFiles()
        if (includeCryptoDb) {
            cacheSize + sessionDirectory.fileDirectory.getSizeOfFiles()
        } else {
            cacheSize + listOf(
                "matrix-sdk-state.sqlite3",
                "matrix-sdk-state.sqlite3-shm",
                "matrix-sdk-state.sqlite3-wal",
            ).map { fileName ->
                File(sessionDirectory.fileDirectory, fileName)
            }.sumOf { file ->
                file.length()
            }
        }
    }

    private suspend fun deleteSessionDirectory(
        deleteCryptoDb: Boolean = false,
    ): Boolean = withContext(sessionDispatcher) {
        val sessionPaths = sessionPathsProvider.provides(sessionId) ?: return@withContext false
        // Always delete the cache directory
        sessionPaths.cacheDirectory.deleteRecursively()
        if (deleteCryptoDb) {
            // Delete the folder and all its content
            sessionPaths.fileDirectory.deleteRecursively()
        } else {
            // Do not delete the crypto database files.
            sessionPaths.fileDirectory.listFiles().orEmpty()
                .filterNot { it.name.contains("matrix-sdk-crypto") }
                .forEach { file ->
                    Timber.w("Deleting file ${file.name}...")
                    file.safeDelete()
                }
            true
        }
    }
}

private val defaultRoomCreationPowerLevels = PowerLevels(
    usersDefault = null,
    eventsDefault = null,
    stateDefault = null,
    ban = null,
    kick = null,
    redact = null,
    invite = null,
    notifications = null,
    users = mapOf(),
    events = mapOf(
        "m.call.member" to 0,
        "org.matrix.msc3401.call.member" to 0,
    )
)
