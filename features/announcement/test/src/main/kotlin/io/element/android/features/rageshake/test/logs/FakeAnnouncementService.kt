/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.test.logs

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.element.android.features.announcement.api.AnnouncementService
import io.element.android.features.announcement.api.AnnouncementState
import io.element.android.tests.testutils.lambda.lambdaError

class FakeAnnouncementService(
    val onEnteringSpaceTabResult: () -> Unit = { lambdaError() },
    val renderResult: (AnnouncementState, Modifier) -> Unit = { _, _ -> lambdaError() },
) : AnnouncementService {
    override suspend fun onEnteringSpaceTab() {
        onEnteringSpaceTabResult()
    }

    @Composable
    override fun Render(state: AnnouncementState, modifier: Modifier) {
        renderResult(state, modifier)
    }
}
