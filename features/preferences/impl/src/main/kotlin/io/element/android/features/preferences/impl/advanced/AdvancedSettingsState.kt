/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.advanced

import io.element.android.compound.theme.Theme
import io.element.android.libraries.matrix.api.media.MediaPreviewValue

data class AdvancedSettingsState(
    val isDeveloperModeEnabled: Boolean,
    val isSharePresenceEnabled: Boolean,
    val doesCompressMedia: Boolean,
    val theme: Theme,
    val showChangeThemeDialog: Boolean,
    val hideInviteAvatars: Boolean,
    val timelineMediaPreviewValue: MediaPreviewValue,
    val eventSink: (AdvancedSettingsEvents) -> Unit
)
