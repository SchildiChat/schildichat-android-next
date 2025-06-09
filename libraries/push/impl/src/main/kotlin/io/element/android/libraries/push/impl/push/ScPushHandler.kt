/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.push

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.IconCompat
import chat.schildi.lib.preferences.ScAppStateStore
import chat.schildi.lib.preferences.ScPreferencesStore
import chat.schildi.lib.preferences.ScPrefs
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.call.api.CallType
import io.element.android.features.call.api.ElementCallEntryPoint
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.push.impl.history.PushHistoryService
import io.element.android.libraries.push.impl.history.onDiagnosticPush
import io.element.android.libraries.push.impl.history.onInvalidPushReceived
import io.element.android.libraries.push.impl.history.onSuccess
import io.element.android.libraries.push.impl.history.onUnableToResolveEvent
import io.element.android.libraries.push.impl.history.onUnableToRetrieveSession
import io.element.android.libraries.push.impl.history.scOnDeferredPushHandling
import io.element.android.libraries.push.impl.history.scOnException
import io.element.android.libraries.push.impl.notifications.NotifiableEventResolver
import io.element.android.libraries.push.impl.notifications.NotificationEventRequest
import io.element.android.libraries.push.impl.notifications.channels.NotificationChannels
import io.element.android.libraries.push.impl.notifications.channels.SC_NOTIFICATION_FAILURE_NOTIFICATION_CHANNEL_ID
import io.element.android.libraries.push.impl.notifications.factories.PendingIntentFactory
import io.element.android.libraries.push.impl.notifications.model.NotifiableRingingCallEvent
import io.element.android.libraries.push.impl.notifications.model.ResolvedPushEvent
import io.element.android.libraries.push.impl.test.DefaultTestPush
import io.element.android.libraries.push.impl.troubleshoot.DiagnosticPushHandler
import io.element.android.libraries.pushproviders.api.PushData
import io.element.android.libraries.pushproviders.api.PushHandler
import io.element.android.libraries.pushstore.api.UserPushStoreFactory
import io.element.android.libraries.pushstore.api.clientsecret.PushClientSecret
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject

private val loggerTag = LoggerTag("ScPushHandler", LoggerTag.PushLoggerTag)

/**
 * Copy of DefaultPushHandler from v25.05.4 in order to drop event batch processing, we want per-message workers for reliability.
 * TODO can we make the diff to upstream smaller again somehow? E.g. have the worker to listen for the first notification queue result for the query, and re-insert if necesarry?
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class ScPushHandler @Inject constructor(
    private val onNotifiableEventReceived: OnNotifiableEventReceived,
    private val onRedactedEventReceived: OnRedactedEventReceived,
    private val notifiableEventResolver: NotifiableEventResolver,
    private val incrementPushDataStore: IncrementPushDataStore,
    private val userPushStoreFactory: UserPushStoreFactory,
    private val scAppStateStore: ScAppStateStore,
    private val scPreferencesStore: ScPreferencesStore,
    private val pushClientSecret: PushClientSecret,
    private val buildMeta: BuildMeta,
    private val matrixAuthenticationService: MatrixAuthenticationService,
    private val diagnosticPushHandler: DiagnosticPushHandler,
    private val elementCallEntryPoint: ElementCallEntryPoint,
    private val notificationChannels: NotificationChannels,
    private val pushHistoryService: PushHistoryService,
    private val upstreamPushHandler: DefaultPushHandler,
    private val notificationManager: NotificationManagerCompat,
    private val pendingIntentFactory: PendingIntentFactory,
    @ApplicationContext
    private val context: Context,
) : PushHandler {

    private suspend fun shouldUseUpstream() = scPreferencesStore.settingFlow(ScPrefs.NOTIFICATION_WORKER).first().not()

    /**
     * Called when message is received.
     *
     * @param pushData the data received in the push.
     * @param providerInfo the provider info.
     */
    override suspend fun handle(pushData: PushData, providerInfo: String): Boolean {
        if (shouldUseUpstream()) {
            return upstreamPushHandler.handle(pushData, providerInfo)
        }
        Timber.tag(loggerTag.value).d("## handling pushData: ${pushData.roomId}/${pushData.eventId}")
        if (buildMeta.lowPrivacyLoggingEnabled) {
            Timber.tag(loggerTag.value).d("## pushData: $pushData")
        }
        //incrementPushDataStore.incrementPushCounter() // SC: Moved to scHandleReceived()
        // Diagnostic Push
        return if (pushData.eventId == DefaultTestPush.TEST_EVENT_ID) {
            pushHistoryService.onDiagnosticPush(providerInfo)
            diagnosticPushHandler.handlePush()
            true
        } else {
            handleInternal(pushData, providerInfo)
        }
    }

    override suspend fun handleInvalid(providerInfo: String, data: String) {
        if (shouldUseUpstream()) {
            return upstreamPushHandler.handleInvalid(providerInfo, data)
        }
        //incrementPushDataStore.incrementPushCounter() // SC: Moved to scHandleReceived()
        pushHistoryService.onInvalidPushReceived(providerInfo, data)
    }

    override suspend fun scHandleReceived() = incrementPushDataStore.incrementPushCounter()
    override suspend fun scHandleDeferred(providerInfo: String, pushData: PushData?) =
        pushHistoryService.scOnDeferredPushHandling(providerInfo, pushData)

    override suspend fun scHandleLookupFailure(providerInfo: String, pushData: PushData) {
        if (!scPreferencesStore.settingFlow(ScPrefs.NOTIFY_FAILED_NOTIFICATION_LOOKUP).first()) return
        val clientSecret = pushData.clientSecret
        // clientSecret should not be null. If this happens, restore default session
        val userId = clientSecret?.let {
            // Get userId from client secret
            pushClientSecret.getUserIdFromSecret(clientSecret)
        } ?: matrixAuthenticationService.getLatestSessionId()
        if (userId == null) {
            Timber.w("Unable to get a session on push failure")
        }
        val notification = NotificationCompat.Builder(context, SC_NOTIFICATION_FAILURE_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(IconCompat.createWithResource(context, CommonDrawables.ic_notification))
            .setContentTitle(context.getString(chat.schildi.lib.R.string.sc_notification_lookup_failure_title))
            .setContentText(context.getString(chat.schildi.lib.R.string.sc_notification_lookup_failure_summary))
            .setContentIntent(userId?.let { pendingIntentFactory.createOpenSessionPendingIntent(userId) })
            .setAutoCancel(true)
            .build()
        runCatchingExceptions {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Timber.tag(loggerTag.value).w("Failed to notify about notification failure, missing permission")
                return
            }
            notificationManager.notify("FAILED_PUSH_LOOKUP".hashCode(), notification)
        }.onFailure {
            Timber.tag(loggerTag.value).e(it, "Failed to notify about notification failure")
        }
    }

    /**
     * Internal receive method.
     *
     * @param pushData Object containing message data.
     * @param providerInfo the provider info.
     *
     * @return true if handling was successful / should not retry. SC addition.
     */
    private suspend fun handleInternal(pushData: PushData, providerInfo: String): Boolean {
        return try {
            if (buildMeta.lowPrivacyLoggingEnabled) {
                Timber.tag(loggerTag.value).d("## handleInternal() : $pushData")
            } else {
                Timber.tag(loggerTag.value).d("## handleInternal()")
            }
            val clientSecret = pushData.clientSecret
            // clientSecret should not be null. If this happens, restore default session
            var reason = if (clientSecret == null) "No client secret" else ""
            val userId = clientSecret?.let {
                // Get userId from client secret
                pushClientSecret.getUserIdFromSecret(clientSecret).also {
                    if (it == null) {
                        reason = "Unable to get userId from client secret"
                    }
                }
            }
                ?: run {
                    matrixAuthenticationService.getLatestSessionId().also {
                        if (it == null) {
                            if (reason.isNotEmpty()) reason += " - "
                            reason += "Unable to get latest sessionId"
                        }
                    }
                }
            if (userId == null) {
                Timber.w("Unable to get a session")
                pushHistoryService.onUnableToRetrieveSession(
                    providerInfo = providerInfo,
                    eventId = pushData.eventId,
                    roomId = pushData.roomId,
                    reason = reason,
                )
                return true
            }
            val request = NotificationEventRequest(sessionId = userId, roomId = pushData.roomId, eventId = pushData.eventId, providerInfo = providerInfo)
            notifiableEventResolver.resolveEvents(userId, listOf(request)).fold(
                onSuccess = { resolvedPushEvents ->
                    resolvedPushEvents.forEach { (request, result) ->
                        val resolvedPushEvent = result.getOrNull()
                        pushHistoryService.onSuccess(
                            providerInfo = providerInfo,
                            eventId = pushData.eventId,
                            roomId = pushData.roomId,
                            sessionId = userId,
                            comment = resolvedPushEvent?.javaClass?.simpleName.toString(),
                        )

                        when (resolvedPushEvent) {
                            is ResolvedPushEvent.Event -> {
                                when (val notifiableEvent = resolvedPushEvent.notifiableEvent) {
                                    is NotifiableRingingCallEvent -> {
                                        Timber.tag(loggerTag.value).d("Notifiable event ${pushData.eventId} is ringing call: $notifiableEvent")
                                        onNotifiableEventReceived.onNotifiableEventsReceived(listOf(notifiableEvent))
                                        handleRingingCallEvent(notifiableEvent)
                                    }
                                    else -> {
                                        Timber.tag(loggerTag.value).d("Notifiable event ${pushData.eventId} is normal event: $notifiableEvent")
                                        val userPushStore = userPushStoreFactory.getOrCreate(userId)
                                        val areNotificationsEnabled = userPushStore.getNotificationEnabledForDevice().first()
                                        if (areNotificationsEnabled) {
                                            onNotifiableEventReceived.onNotifiableEventsReceived(listOf(notifiableEvent))
                                        } else {
                                            Timber.tag(loggerTag.value).i("Notification are disabled for this device, ignore push.")
                                        }
                                        scAppStateStore.onPushReceived(userPushStore.getPushProviderName())
                                    }
                                }
                            }
                            is ResolvedPushEvent.Redaction -> {
                                onRedactedEventReceived.onRedactedEventsReceived(listOf(resolvedPushEvent))
                            }
                            null -> {}
                        }
                    }
                    resolvedPushEvents.any { it.value.isSuccess }
                },
                onFailure = { failure ->
                    Timber.tag(loggerTag.value).w(failure, "Unable to get a notification data")
                    pushHistoryService.onUnableToResolveEvent(
                        providerInfo = providerInfo,
                        eventId = pushData.eventId,
                        roomId = pushData.roomId,
                        sessionId = userId,
                        reason = failure.message ?: failure.javaClass.simpleName,
                    )
                    false
                }
            )
        } catch (e: Exception) {
            Timber.tag(loggerTag.value).e(e, "## handleInternal() failed")
            pushHistoryService.scOnException(providerInfo, pushData, null, (e.message ?: e.javaClass.name))
            false
        }
    }

    private suspend fun handleRingingCallEvent(notifiableEvent: NotifiableRingingCallEvent) {
        Timber.i("## handleInternal() : Incoming call.")
        elementCallEntryPoint.handleIncomingCall(
            callType = CallType.RoomCall(notifiableEvent.sessionId, notifiableEvent.roomId),
            eventId = notifiableEvent.eventId,
            senderId = notifiableEvent.senderId,
            roomName = notifiableEvent.roomName,
            senderName = notifiableEvent.senderDisambiguatedDisplayName,
            avatarUrl = notifiableEvent.roomAvatarUrl,
            timestamp = notifiableEvent.timestamp,
            notificationChannelId = notificationChannels.getChannelForIncomingCall(ring = true),
            textContent = notifiableEvent.description,
        )
    }
}
