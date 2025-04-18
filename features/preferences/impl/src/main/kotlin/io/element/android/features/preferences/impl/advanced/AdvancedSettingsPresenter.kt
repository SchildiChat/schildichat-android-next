/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.advanced

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import io.element.android.compound.theme.Theme
import io.element.android.compound.theme.mapToTheme
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.media.MediaPreviewValue
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import io.element.android.libraries.preferences.api.store.SessionPreferencesStore
import kotlinx.coroutines.launch
import javax.inject.Inject

class AdvancedSettingsPresenter @Inject constructor(
    private val appPreferencesStore: AppPreferencesStore,
    private val sessionPreferencesStore: SessionPreferencesStore,
) : Presenter<AdvancedSettingsState> {
    @Composable
    override fun present(): AdvancedSettingsState {
        val localCoroutineScope = rememberCoroutineScope()
        val isDeveloperModeEnabled by remember {
            appPreferencesStore.isDeveloperModeEnabledFlow()
        }.collectAsState(initial = false)
        val isSharePresenceEnabled by remember {
            sessionPreferencesStore.isSharePresenceEnabled()
        }.collectAsState(initial = true)
        val doesCompressMedia by remember {
            sessionPreferencesStore.doesCompressMedia()
        }.collectAsState(initial = true)
        val theme by remember {
            appPreferencesStore.getThemeFlow().mapToTheme()
        }.collectAsState(initial = Theme.System)
        var showChangeThemeDialog by remember { mutableStateOf(false) }

        val hideInviteAvatars by remember {
            appPreferencesStore.getHideInviteAvatarsFlow()
        }.collectAsState(false)

        val timelineMediaPreviewValue by remember {
            appPreferencesStore.getTimelineMediaPreviewValueFlow()
        }.collectAsState(initial = MediaPreviewValue.On)

        fun handleEvents(event: AdvancedSettingsEvents) {
            when (event) {
                is AdvancedSettingsEvents.SetDeveloperModeEnabled -> localCoroutineScope.launch {
                    appPreferencesStore.setDeveloperModeEnabled(event.enabled)
                }
                is AdvancedSettingsEvents.SetSharePresenceEnabled -> localCoroutineScope.launch {
                    sessionPreferencesStore.setSharePresence(event.enabled)
                }
                is AdvancedSettingsEvents.SetCompressMedia -> localCoroutineScope.launch {
                    sessionPreferencesStore.setCompressMedia(event.compress)
                }
                AdvancedSettingsEvents.CancelChangeTheme -> showChangeThemeDialog = false
                AdvancedSettingsEvents.ChangeTheme -> showChangeThemeDialog = true
                is AdvancedSettingsEvents.SetTheme -> localCoroutineScope.launch {
                    appPreferencesStore.setTheme(event.theme.name)
                    showChangeThemeDialog = false
                }
                is AdvancedSettingsEvents.SetHideInviteAvatars -> localCoroutineScope.launch {
                    appPreferencesStore.setHideInviteAvatars(event.value)
                }
                is AdvancedSettingsEvents.SetTimelineMediaPreviewValue -> localCoroutineScope.launch {
                    appPreferencesStore.setTimelineMediaPreviewValue(event.value)
                }
            }
        }

        return AdvancedSettingsState(
            isDeveloperModeEnabled = isDeveloperModeEnabled,
            isSharePresenceEnabled = isSharePresenceEnabled,
            doesCompressMedia = doesCompressMedia,
            theme = theme,
            showChangeThemeDialog = showChangeThemeDialog,
            hideInviteAvatars = hideInviteAvatars,
            timelineMediaPreviewValue = timelineMediaPreviewValue,
            eventSink = { handleEvents(it) }
        )
    }
}
