/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.root

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.logout.api.direct.DirectLogoutEvents
import io.element.android.features.logout.api.direct.DirectLogoutView
import io.element.android.libraries.androidutils.browser.openUrlInChromeCustomTab
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.user.MatrixUser

@ContributesNode(SessionScope::class)
@AssistedInject
class PreferencesRootNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: PreferencesRootPresenter,
    private val directLogoutView: DirectLogoutView,
) : Node(buildContext, plugins = plugins) {
    interface Callback : Plugin {
        fun navigateToAddAccount()
        fun navigateToBugReport()
        fun navigateToSecureBackup()
        fun navigateToAnalyticsSettings()
        fun navigateToAbout()
        fun navigateToDeveloperSettings()
        fun navigateToNotificationSettings()
        fun navigateToLockScreenSettings()
        fun navigateToAdvancedSettings()
        fun navigateToLabs()
        fun navigateToUserProfile(matrixUser: MatrixUser)
        fun navigateToBlockedUsers()
        fun startSignOutFlow()
        fun startAccountDeactivationFlow()
    }

    private fun onAddAccount() {
        plugins<Callback>().forEach { it.navigateToAddAccount() }
    }

    private fun onOpenBugReport() {
        plugins<Callback>().forEach { it.navigateToBugReport() }
    }

    private fun onSecureBackupClick() {
        plugins<Callback>().forEach { it.navigateToSecureBackup() }
    }

    private fun onOpenDeveloperSettings() {
        plugins<Callback>().forEach { it.navigateToDeveloperSettings() }
    }

    private fun onOpenAdvancedSettings() {
        plugins<Callback>().forEach { it.navigateToAdvancedSettings() }
    }

    private fun onOpenLabs() {
        plugins<Callback>().forEach { it.navigateToLabs() }
    }

    private fun onOpenAnalytics() {
        plugins<Callback>().forEach { it.navigateToAnalyticsSettings() }
    }

    private fun onOpenAbout() {
        plugins<Callback>().forEach { it.navigateToAbout() }
    }

    private fun onManageAccountClick(
        activity: Activity,
        url: String?,
        isDark: Boolean,
    ) {
        url?.let {
            activity.openUrlInChromeCustomTab(
                null,
                darkTheme = isDark,
                url = it
            )
        }
    }

    private fun onOpenNotificationSettings() {
        plugins<Callback>().forEach { it.navigateToNotificationSettings() }
    }

    private fun onOpenLockScreenSettings() {
        plugins<Callback>().forEach { it.navigateToLockScreenSettings() }
    }

    private fun onOpenUserProfile(matrixUser: MatrixUser) {
        plugins<Callback>().forEach { it.navigateToUserProfile(matrixUser) }
    }

    private fun onOpenBlockedUsers() {
        plugins<Callback>().forEach { it.navigateToBlockedUsers() }
    }

    private fun onSignOutClick() {
        plugins<Callback>().forEach { it.startSignOutFlow() }
    }

    private fun onOpenAccountDeactivation() {
        plugins<Callback>().forEach { it.startAccountDeactivationFlow() }
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        val activity = requireNotNull(LocalActivity.current)
        val isDark = ElementTheme.isLightTheme.not()
        PreferencesRootView(
            state = state,
            modifier = modifier,
            onBackClick = this::navigateUp,
            onAddAccountClick = this::onAddAccount,
            onOpenRageShake = this::onOpenBugReport,
            onOpenAnalytics = this::onOpenAnalytics,
            onOpenAbout = this::onOpenAbout,
            onSecureBackupClick = this::onSecureBackupClick,
            onOpenDeveloperSettings = this::onOpenDeveloperSettings,
            onOpenAdvancedSettings = this::onOpenAdvancedSettings,
            onOpenLabs = this::onOpenLabs,
            onManageAccountClick = { onManageAccountClick(activity, it, isDark) },
            onOpenNotificationSettings = this::onOpenNotificationSettings,
            onOpenLockScreenSettings = this::onOpenLockScreenSettings,
            onOpenUserProfile = this::onOpenUserProfile,
            onOpenBlockedUsers = this::onOpenBlockedUsers,
            onSignOutClick = {
                if (state.directLogoutState.canDoDirectSignOut) {
                    state.directLogoutState.eventSink(DirectLogoutEvents.Logout(ignoreSdkError = false))
                } else {
                    onSignOutClick()
                }
            },
            onDeactivateClick = this::onOpenAccountDeactivation
        )

        directLogoutView.Render(state = state.directLogoutState)
    }
}
