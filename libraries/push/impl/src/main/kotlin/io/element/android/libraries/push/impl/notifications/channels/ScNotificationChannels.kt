package io.element.android.libraries.push.impl.notifications.channels

import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import chat.schildi.lib.R
import io.element.android.services.toolbox.api.strings.StringProvider

// Note, hardcoded also in ScPushHandler
internal const val SC_APP_BG_SERVICE_NOTIFICATION_CHANNEL_ID = "SC_APP_BG_SERVICE_NOTIFICATION_CHANNEL_ID"

fun NotificationManagerCompat.updateScNotificationChannels(stringProvider: StringProvider) {
    createNotificationChannel(
        NotificationChannelCompat.Builder(
            SC_APP_BG_SERVICE_NOTIFICATION_CHANNEL_ID,
            NotificationManagerCompat.IMPORTANCE_MIN,
        )
            .setName(stringProvider.getString(R.string.sc_bg_notification_channel).ifEmpty { "Background service" })
            .setDescription(stringProvider.getString(R.string.sc_bg_notification_channel))
            .setSound(null, null)
            .setLightsEnabled(true)
            .build()
    )
}
