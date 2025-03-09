/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package config

object BuildTimeConfig {
    //const val APPLICATION_ID = "io.element.android.x"
    const val APPLICATION_NAME = "Schildi" // SC: should be overriden by our variants... so use something which will make apparent if that's not happening, where it comes from
    // SC: should be overridden by our variants anyway, but better have below patched anyway...
    const val GOOGLE_APP_ID_RELEASE = "1:326900467720:android:675ae0d20ab67aa73b29bd"
    const val GOOGLE_APP_ID_DEBUG = "1:326900467720:android:675ae0d20ab67aa73b29bd"
    const val GOOGLE_APP_ID_NIGHTLY = "1:326900467720:android:675ae0d20ab67aa73b29bd"
}
