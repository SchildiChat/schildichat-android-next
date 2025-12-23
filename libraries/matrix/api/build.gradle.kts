import config.BuildTimeConfig
import extension.buildConfigFieldStr
import extension.testCommonDependencies

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    id("io.element.android-compose-library")
    id("kotlin-parcelize")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "io.element.android.libraries.matrix.api"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        buildConfigFieldStr(
            name = "CLIENT_URI",
            value = BuildTimeConfig.URL_WEBSITE ?: "https://schildi.chat"
        )
        buildConfigFieldStr(
            name = "LOGO_URI",
            value = BuildTimeConfig.URL_LOGO ?: "https://schildi.chat/img/icon-next.png"
        )
        buildConfigFieldStr(
            name = "TOS_URI",
            value = BuildTimeConfig.URL_ACCEPTABLE_USE ?: "https://schildi.chat/android/next/privacy/"
        )
        buildConfigFieldStr(
            name = "POLICY_URI",
            value = BuildTimeConfig.URL_POLICY ?: "https://schildi.chat/android/next/privacy/"
        )
    }
}

dependencies {
    api(projects.schildi.matrixcore)
    implementation(projects.libraries.di)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.core)
    implementation(projects.services.analytics.api)
    implementation(libs.serialization.json)
    api(projects.libraries.sessionStorage.api)
    implementation(libs.coroutines.core)
    api(projects.libraries.architecture)

    testCommonDependencies(libs)
    testImplementation(projects.libraries.matrix.test)
}
