/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl

import io.element.android.libraries.matrix.api.spaces.SpaceRoom

sealed interface SpaceEvents {
    data object LoadMore : SpaceEvents
    data class Join(val spaceRoom: SpaceRoom): SpaceEvents
}
