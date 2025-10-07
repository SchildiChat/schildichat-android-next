/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.test.logs

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.element.android.features.announcement.api.Announcement
import io.element.android.features.announcement.api.AnnouncementService
import io.element.android.tests.testutils.lambda.lambdaError

class FakeAnnouncementService(
    val showAnnouncementResult: (Announcement) -> Unit = { lambdaError() },
    val renderResult: (Modifier) -> Unit = { lambdaError() },
) : AnnouncementService {
    override suspend fun showAnnouncement(announcement: Announcement) {
        showAnnouncementResult(announcement)
    }

    @Composable
    override fun Render(modifier: Modifier) {
        renderResult(modifier)
    }
}
