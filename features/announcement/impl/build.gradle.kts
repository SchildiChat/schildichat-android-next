import extension.setupDependencyInjection
import extension.testCommonDependencies

/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    id("io.element.android-compose-library")
}

android {
    namespace = "io.element.android.features.announcement.impl"

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

setupDependencyInjection()

dependencies {
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.designsystem)
    implementation(projects.libraries.preferences.api)
    implementation(projects.libraries.uiStrings)
    api(projects.features.announcement.api)
    implementation(libs.androidx.datastore.preferences)

    testCommonDependencies(libs)
}
