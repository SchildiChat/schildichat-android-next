/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    id("io.element.android-compose-library")
}

android {
    namespace = "io.element.android.libraries.ui.utils"

    dependencies {
        testImplementation(libs.test.junit)
        testImplementation(libs.test.truth)
        testImplementation(libs.coroutines.test)
        testImplementation(libs.test.truth)
    }
}
