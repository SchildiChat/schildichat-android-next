/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.announcement.impl.store

import io.element.android.features.announcement.api.Announcement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryAnnouncementStore(
    initialSpaceAnnouncementStatus: AnnouncementStore.AnnouncementStatus = AnnouncementStore.AnnouncementStatus.NeverShown,
    initialNewNotificationSoundAnnouncementStatus: AnnouncementStore.AnnouncementStatus = AnnouncementStore.AnnouncementStatus.NeverShown,
) : AnnouncementStore {
    private val spaceAnnouncement = MutableStateFlow(initialSpaceAnnouncementStatus)
    private val newNotificationSoundAnnouncement = MutableStateFlow(initialNewNotificationSoundAnnouncementStatus)

    override suspend fun setAnnouncementStatus(announcement: Announcement, status: AnnouncementStore.AnnouncementStatus) {
        when (announcement) {
            Announcement.Space -> spaceAnnouncement.value = status
            Announcement.NewNotificationSound -> newNotificationSoundAnnouncement.value = status
        }
    }

    override fun announcementStateFlow(announcement: Announcement): Flow<AnnouncementStore.AnnouncementStatus> {
        return when (announcement) {
            Announcement.Space -> spaceAnnouncement.asStateFlow()
            Announcement.NewNotificationSound -> newNotificationSoundAnnouncement.asStateFlow()
        }
    }

    override suspend fun reset() {
        spaceAnnouncement.value = AnnouncementStore.AnnouncementStatus.NeverShown
    }
}
