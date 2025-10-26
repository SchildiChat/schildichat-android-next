/*
 * Copyright 2021-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications.fake

import coil3.ImageLoader
import io.element.android.libraries.push.impl.notifications.NotificationDataFactory
import io.element.android.libraries.push.impl.notifications.OneShotNotification
import io.element.android.libraries.push.impl.notifications.RoomNotification
import io.element.android.libraries.push.impl.notifications.SummaryNotification
import io.element.android.libraries.push.impl.notifications.factories.NotificationAccountParams
import io.element.android.libraries.push.impl.notifications.fixtures.A_NOTIFICATION
import io.element.android.libraries.push.impl.notifications.model.FallbackNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.InviteNotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableMessageEvent
import io.element.android.libraries.push.impl.notifications.model.SimpleNotifiableEvent
import io.element.android.tests.testutils.lambda.LambdaFiveParamsRecorder
import io.element.android.tests.testutils.lambda.LambdaOneParamRecorder
import io.element.android.tests.testutils.lambda.LambdaThreeParamsRecorder
import io.element.android.tests.testutils.lambda.lambdaRecorder

class FakeNotificationDataFactory(
    var messageEventToNotificationsResult: LambdaThreeParamsRecorder<
        List<NotifiableMessageEvent>, ImageLoader, NotificationAccountParams, List<RoomNotification>
        > = lambdaRecorder { _, _, _ -> emptyList() },
    var summaryToNotificationsResult: LambdaFiveParamsRecorder<
        List<RoomNotification>,
        List<OneShotNotification>,
        List<OneShotNotification>,
        List<OneShotNotification>,
        NotificationAccountParams,
        SummaryNotification
        > = lambdaRecorder { _, _, _, _, _ -> SummaryNotification.Update(A_NOTIFICATION) },
    var inviteToNotificationsResult: LambdaOneParamRecorder<List<InviteNotifiableEvent>, List<OneShotNotification>> = lambdaRecorder { _ -> emptyList() },
    var simpleEventToNotificationsResult: LambdaOneParamRecorder<List<SimpleNotifiableEvent>, List<OneShotNotification>> = lambdaRecorder { _ -> emptyList() },
    var fallbackEventToNotificationsResult: LambdaOneParamRecorder<List<FallbackNotifiableEvent>, List<OneShotNotification>> =
        lambdaRecorder { _ -> emptyList() },
) : NotificationDataFactory {
    override suspend fun List<NotifiableMessageEvent>.toNotifications(
        imageLoader: ImageLoader,
        notificationAccountParams: NotificationAccountParams,
    ): List<RoomNotification> {
        return messageEventToNotificationsResult(this, imageLoader, notificationAccountParams)
    }

    @JvmName("toNotificationInvites")
    @Suppress("INAPPLICABLE_JVM_NAME")
    override fun List<InviteNotifiableEvent>.toNotifications(
        notificationAccountParams: NotificationAccountParams,
    ): List<OneShotNotification> {
        return inviteToNotificationsResult(this)
    }

    @JvmName("toNotificationSimpleEvents")
    @Suppress("INAPPLICABLE_JVM_NAME")
    override fun List<SimpleNotifiableEvent>.toNotifications(
        notificationAccountParams: NotificationAccountParams,
    ): List<OneShotNotification> {
        return simpleEventToNotificationsResult(this)
    }

    @JvmName("toNotificationFallbackEvents")
    @Suppress("INAPPLICABLE_JVM_NAME")
    override fun List<FallbackNotifiableEvent>.toNotifications(
        notificationAccountParams: NotificationAccountParams,
    ): List<OneShotNotification> {
        return fallbackEventToNotificationsResult(this)
    }

    override fun createSummaryNotification(
        roomNotifications: List<RoomNotification>,
        invitationNotifications: List<OneShotNotification>,
        simpleNotifications: List<OneShotNotification>,
        fallbackNotifications: List<OneShotNotification>,
        notificationAccountParams: NotificationAccountParams,
    ): SummaryNotification {
        return summaryToNotificationsResult(
            roomNotifications,
            invitationNotifications,
            simpleNotifications,
            fallbackNotifications,
            notificationAccountParams,
        )
    }
}
