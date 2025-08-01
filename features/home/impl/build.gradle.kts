import extension.setupAnvil

/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    id("io.element.android-compose-library")
    id("kotlin-parcelize")
}

android {
    namespace = "io.element.android.features.home.impl"

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

setupAnvil()

dependencies {
    implementation(projects.schildi.lib)
    implementation(projects.schildi.matrixsdk)
    implementation(projects.schildi.components)
    implementation(projects.schildi.theme)

    implementation(projects.appconfig)
    implementation(projects.libraries.core)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.featureflag.api)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.matrixui)
    implementation(projects.libraries.designsystem)
    implementation(projects.libraries.testtags)
    implementation(projects.libraries.uiStrings)
    implementation(projects.libraries.dateformatter.api)
    implementation(projects.libraries.eventformatter.api)
    implementation(projects.libraries.indicator.api)
    implementation(projects.libraries.deeplink)
    implementation(projects.libraries.fullscreenintent.api)
    implementation(projects.libraries.permissions.api)
    implementation(projects.libraries.permissions.noop)
    implementation(projects.libraries.preferences.api)
    implementation(projects.libraries.push.api)
    implementation(projects.features.invite.api)
    implementation(projects.features.networkmonitor.api)
    implementation(projects.features.logout.api)
    implementation(projects.features.leaveroom.api)
    implementation(projects.features.rageshake.api)
    implementation(projects.services.analytics.api)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.haze)
    implementation(libs.haze.materials)
    implementation(projects.features.reportroom.api)
    api(projects.features.home.api)

    testImplementation(libs.androidx.compose.ui.test.junit)
    testReleaseImplementation(libs.androidx.compose.ui.test.manifest)
    testImplementation(libs.test.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.molecule.runtime)
    testImplementation(libs.test.truth)
    testImplementation(libs.test.turbine)
    testImplementation(libs.test.robolectric)
    testImplementation(projects.features.invite.test)
    testImplementation(projects.features.logout.test)
    testImplementation(projects.features.networkmonitor.test)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.libraries.featureflag.test)
    testImplementation(projects.libraries.dateformatter.test)
    testImplementation(projects.libraries.eventformatter.test)
    testImplementation(projects.libraries.indicator.test)
    testImplementation(projects.libraries.permissions.noop)
    testImplementation(projects.libraries.permissions.test)
    testImplementation(projects.libraries.preferences.test)
    testImplementation(projects.libraries.push.test)
    testImplementation(projects.services.analytics.test)
    testImplementation(projects.services.toolbox.test)
    testImplementation(projects.tests.testutils)
}
