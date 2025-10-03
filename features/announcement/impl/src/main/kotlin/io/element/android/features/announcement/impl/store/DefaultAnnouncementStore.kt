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
import io.element.android.libraries.preferences.api.store.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val spaceAnnouncementKey = intPreferencesKey("spaceAnnouncement")

@ContributesBinding(AppScope::class)
@Inject
class DefaultAnnouncementStore(
    preferenceDataStoreFactory: PreferenceDataStoreFactory,
) : AnnouncementStore {
    private val store = preferenceDataStoreFactory.create("elementx_announcement")

    override suspend fun setSpaceAnnouncementValue(value: AnnouncementStore.SpaceAnnouncement) {
        store.edit {
            it[spaceAnnouncementKey] = value.ordinal
        }
    }

    override fun spaceAnnouncementFlow(): Flow<AnnouncementStore.SpaceAnnouncement> {
        return store.data.map { prefs ->
            val ordinal = prefs[spaceAnnouncementKey] ?: AnnouncementStore.SpaceAnnouncement.NeverShown.ordinal
            AnnouncementStore.SpaceAnnouncement.entries.getOrElse(ordinal) { AnnouncementStore.SpaceAnnouncement.NeverShown }
        }
    }

    override suspend fun reset() {
        store.edit { it.clear() }
    }
}
