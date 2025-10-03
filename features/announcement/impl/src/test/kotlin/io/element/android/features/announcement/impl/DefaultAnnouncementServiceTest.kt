/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.announcement.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.features.announcement.api.Announcement
import io.element.android.features.announcement.impl.spaces.SpaceAnnouncementState
import io.element.android.features.announcement.impl.spaces.aSpaceAnnouncementState
import io.element.android.features.announcement.impl.store.AnnouncementStore
import io.element.android.features.announcement.impl.store.InMemoryAnnouncementStore
import io.element.android.libraries.architecture.Presenter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultAnnouncementServiceTest {
    @Test
    fun `when showing Space announcement, space announcement is set to show only if it was never shown`() = runTest {
        val announcementStore = InMemoryAnnouncementStore()
        val sut = createDefaultAnnouncementService(
            announcementStore = announcementStore,
        )
        assertThat(announcementStore.spaceAnnouncementFlow().first()).isEqualTo(AnnouncementStore.SpaceAnnouncement.NeverShown)
        sut.showAnnouncement(Announcement.Space)
        assertThat(announcementStore.spaceAnnouncementFlow().first()).isEqualTo(AnnouncementStore.SpaceAnnouncement.Show)
        // Simulate user close the announcement
        announcementStore.setSpaceAnnouncementValue(AnnouncementStore.SpaceAnnouncement.Shown)
        // Entering again the space tab should not change the value
        sut.showAnnouncement(Announcement.Space)
        assertThat(announcementStore.spaceAnnouncementFlow().first()).isEqualTo(AnnouncementStore.SpaceAnnouncement.Shown)
    }

    private fun createDefaultAnnouncementService(
        announcementStore: AnnouncementStore = InMemoryAnnouncementStore(),
        announcementPresenter: Presenter<AnnouncementState> = Presenter { anAnnouncementState() },
        spaceAnnouncementPresenter: Presenter<SpaceAnnouncementState> = Presenter { aSpaceAnnouncementState() },
    ) = DefaultAnnouncementService(
        announcementStore = announcementStore,
        announcementPresenter = announcementPresenter,
        spaceAnnouncementPresenter = spaceAnnouncementPresenter,
    )
}
