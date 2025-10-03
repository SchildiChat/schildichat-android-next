/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.announcement.impl.store

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryAnnouncementStore(
    initialSpaceAnnouncement: AnnouncementStore.SpaceAnnouncement = AnnouncementStore.SpaceAnnouncement.NeverShown,
) : AnnouncementStore {
    private val spaceAnnouncement = MutableStateFlow(initialSpaceAnnouncement)
    override suspend fun setSpaceAnnouncementValue(value: AnnouncementStore.SpaceAnnouncement) {
        spaceAnnouncement.value = value
    }

    override fun spaceAnnouncementFlow(): Flow<AnnouncementStore.SpaceAnnouncement> {
        return spaceAnnouncement.asStateFlow()
    }

    override suspend fun reset() {
        spaceAnnouncement.value = AnnouncementStore.SpaceAnnouncement.NeverShown
    }
}
