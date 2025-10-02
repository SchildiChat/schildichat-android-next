/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.announcement.impl.spaces

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

open class SpaceAnnouncementStateProvider : PreviewParameterProvider<SpaceAnnouncementState> {
    override val values: Sequence<SpaceAnnouncementState>
        get() = sequenceOf(
            aSpaceAnnouncementState(),
        )
}

fun aSpaceAnnouncementState(
    applicationName: String = "Element",
    desktopApplicationName: String = "Element",
    eventSink: (SpaceAnnouncementEvents) -> Unit = {},
) = SpaceAnnouncementState(
    applicationName = applicationName,
    desktopApplicationName = desktopApplicationName,
    eventSink = eventSink,
)
