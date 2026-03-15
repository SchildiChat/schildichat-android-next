package io.element.android.libraries.push.impl.push

import io.element.android.libraries.push.api.notifications.conversations.NotificationConversationService
import io.element.android.libraries.push.impl.notifications.model.NotifiableEvent
import io.element.android.libraries.push.impl.notifications.model.NotifiableMessageEvent

suspend fun NotificationConversationService.createNotificationShortcut(notifiableEvents: List<NotifiableEvent>) {
    notifiableEvents
        .asSequence()
        .filterIsInstance<NotifiableMessageEvent>()
        .filter { !it.outGoingMessage && it.threadId == null }
        .distinctBy { it.sessionId to it.roomId }
        .forEach { event ->
            // This is a misnomer. It just creates a chat shortcut.
            onSendMessage(
                sessionId = event.sessionId,
                roomId = event.roomId,
                roomName = event.roomName ?: event.roomId.value,
                roomIsDirect = event.roomIsDm,
                roomAvatarUrl = event.roomAvatarPath,
            )
        }
}
