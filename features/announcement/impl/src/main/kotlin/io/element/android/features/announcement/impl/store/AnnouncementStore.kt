/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.announcement.impl.store

import kotlinx.coroutines.flow.Flow

interface AnnouncementStore {
    suspend fun setSpaceAnnouncementValue(value: SpaceAnnouncement)
    fun spaceAnnouncementFlow(): Flow<SpaceAnnouncement>

    suspend fun reset()

    enum class SpaceAnnouncement {
        NeverShown,
        Show,
        Shown,
    }
}
