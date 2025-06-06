import extension.setupAnvil

/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */
plugins {
    id("io.element.android-compose-library")
}

android {
    namespace = "io.element.android.libraries.voiceplayer.api"
}

setupAnvil()

dependencies {
    implementation(libs.androidx.annotationjvm)
    implementation(libs.coroutines.core)
    implementation(projects.libraries.matrix.api)
}
