/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.announcement.impl.store

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.element.android.features.announcement.api.Announcement
import io.element.android.libraries.preferences.api.store.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val spaceAnnouncementKey = intPreferencesKey("spaceAnnouncement")
private val showNewNotificationSoundBannerKey = intPreferencesKey("showNewNotificationSoundBanner")

@ContributesBinding(AppScope::class)
@Inject
class DefaultAnnouncementStore(
    preferenceDataStoreFactory: PreferenceDataStoreFactory,
) : AnnouncementStore {
    private val store = preferenceDataStoreFactory.create("elementx_announcement")

    override suspend fun setAnnouncementStatus(announcement: Announcement, status: AnnouncementStore.AnnouncementStatus) {
        val key = announcement.toKey()
        store.edit { prefs ->
            prefs[key] = status.ordinal
        }
    }

    override fun announcementStatusFlow(announcement: Announcement): Flow<AnnouncementStore.AnnouncementStatus> {
        val key = announcement.toKey()
        // For NewNotificationSound, a migration will set it to Show on application upgrade (see AppMigration08)
        val defaultStatus = when (announcement) {
            Announcement.Space -> AnnouncementStore.AnnouncementStatus.NeverShown
            Announcement.NewNotificationSound -> AnnouncementStore.AnnouncementStatus.Shown
        }
        return store.data.map { prefs ->
            val ordinal = prefs[key] ?: defaultStatus.ordinal
            AnnouncementStore.AnnouncementStatus.entries.getOrElse(ordinal) { defaultStatus }
        }
    }

    override suspend fun reset() {
        store.edit { it.clear() }
    }
}

private fun Announcement.toKey() = when (this) {
    Announcement.Space -> spaceAnnouncementKey
    Announcement.NewNotificationSound -> showNewNotificationSoundBannerKey
}
