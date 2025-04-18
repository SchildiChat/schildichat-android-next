/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    id("io.element.android-library")
}

android {
    namespace = "io.element.android.libraries.preferences.api"
}

dependencies {
    implementation(projects.schildi.lib)
    implementation(libs.coroutines.core)
    implementation(projects.libraries.matrix.api)
    implementation(libs.androidx.datastore.preferences)

    testImplementation(projects.libraries.preferences.test)
    testImplementation(libs.test.truth)
    testImplementation(libs.coroutines.test)
}
