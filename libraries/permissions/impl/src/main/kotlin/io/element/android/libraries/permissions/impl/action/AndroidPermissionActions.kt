/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.permissions.impl.action

import android.content.Context
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.androidutils.system.startNotificationSettingsIntent
import dev.zacsweers.metro.AppScope
import io.element.android.libraries.di.annotations.ApplicationContext
import dev.zacsweers.metro.Inject

@ContributesBinding(AppScope::class)
@Inject
class AndroidPermissionActions(
    @ApplicationContext private val context: Context
) : PermissionActions {
    override fun openSettings() {
        context.startNotificationSettingsIntent()
    }
}
