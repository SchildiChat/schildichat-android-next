/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.push

import chat.schildi.lib.preferences.ScAppStateStore
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.call.api.CallType
import io.element.android.features.call.api.ElementCallEntryPoint
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService
import io.element.android.libraries.push.impl.notifications.NotifiableEventResolver
import io.element.android.libraries.push.impl.notifications.channels.NotificationChannels
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

private val loggerTag = LoggerTag("PushHandler", LoggerTag.PushLoggerTag)

@ContributesBinding(AppScope::class)
class DefaultPushHandler @Inject constructor(
    private val onNotifiableEventReceived: OnNotifiableEventReceived,
    private val onRedactedEventReceived: OnRedactedEventReceived,
    private val notifiableEventResolver: NotifiableEventResolver,
    private val incrementPushDataStore: IncrementPushDataStore,
    private val userPushStoreFactory: UserPushStoreFactory,
    private val scAppStateStore: ScAppStateStore,
    private val pushClientSecret: PushClientSecret,
    private val buildMeta: BuildMeta,
    private val matrixAuthenticationService: MatrixAuthenticationService,
    private val diagnosticPushHandler: DiagnosticPushHandler,
    private val elementCallEntryPoint: ElementCallEntryPoint,
    private val notificationChannels: NotificationChannels,
) : PushHandler {
    /**
     * Called when message is received.
     *
     * @param pushData the data received in the push.
     */
    override suspend fun handle(pushData: PushData) {
        Timber.tag(loggerTag.value).d("## handling pushData: ${pushData.roomId}/${pushData.eventId}")
        if (buildMeta.lowPrivacyLoggingEnabled) {
            Timber.tag(loggerTag.value).d("## pushData: $pushData")
        }
        incrementPushDataStore.incrementPushCounter()
        // Diagnostic Push
        if (pushData.eventId == DefaultTestPush.TEST_EVENT_ID) {
            diagnosticPushHandler.handlePush()
        } else {
            handleInternal(pushData)
        }
    }

    /**
     * Internal receive method.
     *
     * @param pushData Object containing message data.
     */
    private suspend fun handleInternal(pushData: PushData) {
        try {
            if (buildMeta.lowPrivacyLoggingEnabled) {
                Timber.tag(loggerTag.value).d("## handleInternal() : $pushData")
            } else {
                Timber.tag(loggerTag.value).d("## handleInternal()")
            }
            val clientSecret = pushData.clientSecret
            // clientSecret should not be null. If this happens, restore default session
            val userId = clientSecret
                ?.let {
                    // Get userId from client secret
                    pushClientSecret.getUserIdFromSecret(clientSecret)
                }
                ?: run {
                    matrixAuthenticationService.getLatestSessionId()
                }
            if (userId == null) {
                Timber.w("Unable to get a session")
                return
            }
            val resolvedPushEvent = notifiableEventResolver.resolveEvent(userId, pushData.roomId, pushData.eventId)
            when (resolvedPushEvent) {
                null -> Timber.tag(loggerTag.value).w("Unable to get a notification data")
                is ResolvedPushEvent.Event -> {
                    when (val notifiableEvent = resolvedPushEvent.notifiableEvent) {
                        is NotifiableRingingCallEvent -> {
                            onNotifiableEventReceived.onNotifiableEventReceived(notifiableEvent)
                            handleRingingCallEvent(notifiableEvent)
                        }
                        else -> {
                            val userPushStore = userPushStoreFactory.getOrCreate(userId)
                            val areNotificationsEnabled = userPushStore.getNotificationEnabledForDevice().first()
                            if (areNotificationsEnabled) {
                                onNotifiableEventReceived.onNotifiableEventReceived(notifiableEvent)
                            } else {
                                Timber.tag(loggerTag.value).i("Notification are disabled for this device, ignore push.")
                            }
                            scAppStateStore.onPushReceived(userPushStore.getPushProviderName())
                        }
                    }
                }
                is ResolvedPushEvent.Redaction -> {
                    onRedactedEventReceived.onRedactedEventReceived(resolvedPushEvent)
                }
            }
        } catch (e: Exception) {
            Timber.tag(loggerTag.value).e(e, "## handleInternal() failed")
        }
    }

    private fun handleRingingCallEvent(notifiableEvent: NotifiableRingingCallEvent) {
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
