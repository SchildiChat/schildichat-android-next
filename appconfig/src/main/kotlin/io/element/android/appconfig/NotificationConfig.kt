/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.appconfig

import android.graphics.Color
import androidx.annotation.ColorInt

object NotificationConfig {
    // TODO EAx Implement and set to true at some point
    const val SUPPORT_MARK_AS_READ_ACTION = false

    // TODO EAx Implement and set to true at some point
    const val SUPPORT_JOIN_DECLINE_INVITE = false

    // TODO EAx Implement and set to true at some point
    const val SUPPORT_QUICK_REPLY_ACTION = false

    @ColorInt
    val NOTIFICATION_ACCENT_COLOR: Int = Color.parseColor("#FF0A5C7C")
}
