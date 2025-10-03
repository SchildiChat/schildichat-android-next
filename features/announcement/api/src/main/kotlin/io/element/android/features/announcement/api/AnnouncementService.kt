/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.announcement.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface AnnouncementService {
    suspend fun showAnnouncement(announcement: Announcement)

    @Composable
    fun Render(
        modifier: Modifier,
    )
}
