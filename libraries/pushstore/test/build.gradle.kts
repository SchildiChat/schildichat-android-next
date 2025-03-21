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
    namespace = "io.element.android.libraries.pushstore.test"
}

dependencies {
    api(projects.libraries.matrix.api)
    api(libs.coroutines.core)
    implementation(libs.coroutines.test)
    implementation(projects.tests.testutils)
    implementation(projects.libraries.pushstore.api)
}
