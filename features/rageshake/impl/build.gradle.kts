/*
 * Copyright (c) 2022 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
    id("io.element.android-compose-library")
    alias(libs.plugins.anvil)
    id("kotlin-parcelize")
}

android {
    namespace = "io.element.android.features.rageshake.impl"

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

anvil {
    generateDaggerFactories.set(true)
}

dependencies {
    implementation(projects.schildi.lib)
    implementation(projects.anvilannotations)
    anvil(projects.anvilcodegen)
    implementation(projects.appconfig)
    implementation(projects.services.toolbox.api)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.core)
    implementation(projects.libraries.network)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.designsystem)
    implementation(projects.libraries.uiStrings)
    implementation(projects.libraries.sessionStorage.api)
    implementation(projects.libraries.matrix.api)
    api(libs.squareup.seismic)
    api(projects.features.rageshake.api)
    implementation(libs.androidx.datastore.preferences)
    implementation(platform(libs.network.okhttp.bom))
    implementation(libs.network.okhttp.okhttp)
    implementation(libs.coil)
    implementation(libs.coil.compose)

    testImplementation(libs.test.junit)
    testImplementation(libs.test.robolectric)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.molecule.runtime)
    testImplementation(libs.test.truth)
    testImplementation(libs.test.turbine)
    testImplementation(libs.test.mockk)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.libraries.sessionStorage.implMemory)
    testImplementation(projects.features.rageshake.test)
    testImplementation(projects.tests.testutils)
    testImplementation(projects.services.toolbox.test)
    testImplementation(libs.network.mockwebserver)
}
