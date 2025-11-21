/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.architecture.navigation

import com.bumble.appyx.core.plugin.Plugin

/**
 * Base callback interface to be implemented by callers to handle results from nodes.
 */
fun interface BaseCallback : Plugin {
    fun onDone()
}
