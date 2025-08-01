/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.fullscreenintent.impl

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.fullscreenintent.api.FullScreenIntentPermissionsEvents
import io.element.android.libraries.fullscreenintent.api.FullScreenIntentPermissionsState
import io.element.android.libraries.preferences.api.store.PreferenceDataStoreFactory
import io.element.android.services.toolbox.api.intent.ExternalIntentLauncher
import io.element.android.services.toolbox.api.sdk.BuildVersionSdkIntProvider
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@SingleIn(AppScope::class)
class FullScreenIntentPermissionsPresenter @Inject constructor(
    private val buildVersionSdkIntProvider: BuildVersionSdkIntProvider,
    private val externalIntentLauncher: ExternalIntentLauncher,
    private val buildMeta: BuildMeta,
    private val notificationManagerCompat: NotificationManagerCompat,
    preferencesDataStoreFactory: PreferenceDataStoreFactory,
) : Presenter<FullScreenIntentPermissionsState> {
    companion object {
        private const val PREF_KEY_FULL_SCREEN_INTENT_BANNER_DISMISSED = "PREF_KEY_FULL_SCREEN_INTENT_BANNER_DISMISSED"
    }

    private val dataStore = preferencesDataStoreFactory.create("full_screen_intent_permissions")

    private val isFullScreenIntentBannerDismissed = dataStore.data.map { prefs ->
        prefs[booleanPreferencesKey(PREF_KEY_FULL_SCREEN_INTENT_BANNER_DISMISSED)] ?: false
    }

    private suspend fun dismissFullScreenIntentBanner() {
        dataStore.edit { prefs ->
            prefs[booleanPreferencesKey(PREF_KEY_FULL_SCREEN_INTENT_BANNER_DISMISSED)] = true
        }
    }

    @Composable
    override fun present(): FullScreenIntentPermissionsState {
        val coroutineScope = rememberCoroutineScope()
        val isGranted = notificationManagerCompat.canUseFullScreenIntent()
        val isBannerDismissed by isFullScreenIntentBannerDismissed.collectAsState(initial = true)

        fun handleEvents(event: FullScreenIntentPermissionsEvents) {
            when (event) {
                FullScreenIntentPermissionsEvents.Dismiss -> coroutineScope.launch {
                    dismissFullScreenIntentBanner()
                }
                FullScreenIntentPermissionsEvents.OpenSettings -> openFullScreenIntentSettings()
            }
        }

        return FullScreenIntentPermissionsState(
            permissionGranted = isGranted,
            shouldDisplayBanner = !isBannerDismissed && !isGranted,
            eventSink = ::handleEvents,
        )
    }

    private fun openFullScreenIntentSettings() {
        if (buildVersionSdkIntProvider.isAtLeast(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)) {
            try {
                val intent = Intent(
                    Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,
                    "package:${buildMeta.applicationId}".toUri()
                )
                externalIntentLauncher.launch(intent)
            } catch (e: ActivityNotFoundException) {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, buildMeta.applicationId)
                externalIntentLauncher.launch(intent)
            }
        }
    }
}
