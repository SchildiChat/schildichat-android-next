/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl

import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.location.api.BuildConfig
import io.element.android.features.location.api.LocationService
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject

@ContributesBinding(AppScope::class)
@Inject
class DefaultLocationService() : LocationService {
    override fun isServiceAvailable(): Boolean {
        return BuildConfig.MAPTILER_API_KEY.isNotEmpty()
    }
}
