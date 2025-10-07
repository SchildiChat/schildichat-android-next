/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.migration.impl.migrations

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.preferences.test.InMemoryAppPreferencesStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AppMigration08Test {
    @Test
    fun `migration on fresh install should not modify the store`() = runTest {
        val store = InMemoryAppPreferencesStore()
        assertThat(store.showNewNotificationSoundBanner().first()).isFalse()
        val migration = AppMigration08(store)
        migration.migrate(isFreshInstall = true)
        assertThat(store.showNewNotificationSoundBanner().first()).isFalse()
    }

    @Test
    fun `migration on upgrade should modify the store`() = runTest {
        val store = InMemoryAppPreferencesStore()
        assertThat(store.showNewNotificationSoundBanner().first()).isFalse()
        val migration = AppMigration08(store)
        migration.migrate(isFreshInstall = false)
        assertThat(store.showNewNotificationSoundBanner().first()).isTrue()
    }
}
