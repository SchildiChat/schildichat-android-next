/*
 * Copyright 2023, 2024 New Vector Ltd.
 * Copyright 2021 Google LLC
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.maplibre.compose

import android.view.Gravity
import androidx.compose.ui.graphics.Color

internal val DefaultMapUiSettings = MapUiSettings()

/**
 * Data class for UI-related settings on the map.
 *
 * Note: Should not be a data class if in need of maintaining binary compatibility
 * on future changes. See: https://jakewharton.com/public-api-challenges-in-kotlin/
 */
public data class MapUiSettings(
    public val compassEnabled: Boolean = true,
    public val rotationGesturesEnabled: Boolean = true,
    public val scrollGesturesEnabled: Boolean = true,
    public val tiltGesturesEnabled: Boolean = true,
    public val zoomGesturesEnabled: Boolean = true,
    public val logoGravity: Int = Gravity.BOTTOM,
    public val attributionGravity: Int = Gravity.BOTTOM,
    public val attributionTintColor: Color = Color.Unspecified,
)
