/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    id("io.element.android-compose-library")
    alias(libs.plugins.ksp)
    id("kotlin-parcelize")
}

android {
    namespace = "io.element.android.libraries.designsystem"

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        getByName("release") {
            consumerProguardFiles("consumer-rules.pro")
        }
    }

    dependencies {
        // SC lib and theme required as API for compose previews using ElementPreview
        api(projects.schildi.lib)
        api(projects.schildi.theme)
        api(libs.compound)

        implementation(libs.androidx.compose.material3.windowsizeclass)
        implementation(libs.androidx.compose.material3.adaptive)
        implementation(libs.coil.compose)
        implementation(libs.vanniktech.blurhash)
        implementation(projects.features.enterprise.api)
        implementation(projects.libraries.architecture)
        implementation(projects.libraries.core)
        implementation(projects.libraries.preferences.api)
        implementation(projects.libraries.testtags)
        implementation(projects.libraries.uiStrings)

        ksp(libs.showkase.processor)

        testImplementation(libs.test.junit)
        testImplementation(libs.coroutines.test)
        testImplementation(libs.molecule.runtime)
        testImplementation(libs.test.truth)
        testImplementation(libs.test.turbine)
    }
}

// SC resource override
android {
    // Use a flavor for common things that the upstream config will not override by the build type
    flavorDimensions += "package"
    productFlavors {
        create("sc") {
            dimension = "package"
        }
    }
}
