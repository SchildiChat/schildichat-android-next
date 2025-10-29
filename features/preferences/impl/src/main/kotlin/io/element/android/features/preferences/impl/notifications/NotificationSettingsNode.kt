/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.notifications

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.libraries.di.SessionScope

@ContributesNode(SessionScope::class)
@AssistedInject
class NotificationSettingsNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: NotificationSettingsPresenter,
) : Node(buildContext, plugins = plugins) {
    interface Callback : Plugin {
        fun navigateToEditDefaultNotificationSetting(isOneToOne: Boolean)
        fun navigateToTroubleshootNotifications()
    }

    private val callbacks = plugins<Callback>()

    private fun navigateToEditDefaultNotificationSetting(isOneToOne: Boolean) {
        callbacks.forEach { it.navigateToEditDefaultNotificationSetting(isOneToOne) }
    }

    private fun navigateToTroubleshootNotifications() {
        callbacks.forEach { it.navigateToTroubleshootNotifications() }
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        NotificationSettingsView(
            state = state,
            onOpenEditDefault = ::navigateToEditDefaultNotificationSetting,
            onBackClick = ::navigateUp,
            onTroubleshootNotificationsClick = ::navigateToTroubleshootNotifications,
            modifier = modifier,
        )
    }
}
