/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.announcement.api

data class AnnouncementState(
    val showSpaceAnnouncement: Boolean,
)

fun anAnnouncementState(
    showSpaceAnnouncement: Boolean = false,
) = AnnouncementState(
    showSpaceAnnouncement = showSpaceAnnouncement,
)
