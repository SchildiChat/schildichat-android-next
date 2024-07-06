package io.element.android.libraries.push.impl.notifications.factories

import android.graphics.Color
import io.element.android.appconfig.NotificationConfig
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.core.meta.BuildType
import io.element.android.libraries.core.meta.isInternalBuild

val BuildMeta.scNotificationColor
    get() = when {
        buildType == BuildType.DEBUG_SC -> Color.parseColor("#FFFFFFFF")
        isInternalBuild -> Color.parseColor("#FF8BC34A")
        buildType == BuildType.RELEASE_SC -> NotificationConfig.NOTIFICATION_ACCENT_COLOR
        else -> null
    }
