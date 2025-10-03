/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.announcement.impl.spaces

import com.google.common.truth.Truth.assertThat
import io.element.android.features.announcement.impl.store.AnnouncementStore
import io.element.android.features.announcement.impl.store.InMemoryAnnouncementStore
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.matrix.test.AN_APPLICATION_NAME
import io.element.android.libraries.matrix.test.AN_APPLICATION_NAME_DESKTOP
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.tests.testutils.test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SpaceAnnouncementPresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val presenter = createSpaceAnnouncementPresenter()
        presenter.test {
            val state = awaitItem()
            assertThat(state.applicationName).isEqualTo(AN_APPLICATION_NAME)
            assertThat(state.desktopApplicationName).isEqualTo(AN_APPLICATION_NAME_DESKTOP)
        }
    }

    @Test
    fun `present - when user continues, the store is updated`() = runTest {
        val store = InMemoryAnnouncementStore()
        val presenter = createSpaceAnnouncementPresenter(
            announcementStore = store,
        )
        presenter.test {
            assertThat(store.spaceAnnouncementFlow().first()).isEqualTo(AnnouncementStore.SpaceAnnouncement.NeverShown)
            val state = awaitItem()
            state.eventSink(SpaceAnnouncementEvents.Continue)
            assertThat(store.spaceAnnouncementFlow().first()).isEqualTo(AnnouncementStore.SpaceAnnouncement.Shown)
        }
    }
}

private fun createSpaceAnnouncementPresenter(
    buildMeta: BuildMeta = aBuildMeta(
        applicationName = AN_APPLICATION_NAME,
        desktopApplicationName = AN_APPLICATION_NAME_DESKTOP,
    ),
    announcementStore: AnnouncementStore = InMemoryAnnouncementStore(),
) = SpaceAnnouncementPresenter(
    buildMeta = buildMeta,
    announcementStore = announcementStore,
)
