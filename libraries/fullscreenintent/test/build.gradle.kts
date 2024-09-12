/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

plugins {
    id("io.element.android-compose-library")
}

android {
    namespace = "io.element.android.libraries.fullscreenintent.test"
}

dependencies {
    api(projects.libraries.fullscreenintent.api)
    implementation(projects.libraries.architecture)
}