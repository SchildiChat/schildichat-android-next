/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.announcement.impl

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.element.android.features.announcement.api.AnnouncementService
import io.element.android.features.announcement.api.AnnouncementState
import io.element.android.features.announcement.impl.spaces.SpaceAnnouncementState
import io.element.android.features.announcement.impl.spaces.SpaceAnnouncementView
import io.element.android.features.announcement.impl.store.AnnouncementStore
import io.element.android.libraries.architecture.Presenter
import kotlinx.coroutines.flow.first

@ContributesBinding(AppScope::class)
@Inject
class DefaultAnnouncementService(
    private val announcementStore: AnnouncementStore,
    private val spaceAnnouncementPresenter: Presenter<SpaceAnnouncementState>,
) : AnnouncementService {
    override suspend fun onEnteringSpaceTab() {
        val currentValue = announcementStore.spaceAnnouncementFlow().first()
        if (currentValue == AnnouncementStore.SpaceAnnouncement.NeverShown) {
            announcementStore.setSpaceAnnouncementValue(AnnouncementStore.SpaceAnnouncement.Show)
        }
    }

    @Composable
    override fun Render(state: AnnouncementState, modifier: Modifier) {
        Box(modifier = modifier.fillMaxSize()) {
            AnimatedVisibility(
                visible = state.showSpaceAnnouncement,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                val spaceAnnouncementState = spaceAnnouncementPresenter.present()
                SpaceAnnouncementView(
                    state = spaceAnnouncementState,
                )
            }
        }
    }
}
