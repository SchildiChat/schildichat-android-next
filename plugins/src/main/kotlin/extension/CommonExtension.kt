/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package extension

import Versions
import com.android.build.api.dsl.CommonExtension
import isEnterpriseBuild
import org.gradle.api.Project
import java.io.File

fun CommonExtension<*, *, *, *, *, *>.androidConfig(project: Project) {
    defaultConfig {
        compileSdk = Versions.COMPILE_SDK
        minSdk = Versions.minSdk
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
            generatedDensities()
        }
    }

    compileOptions {
        sourceCompatibility = Versions.javaVersion
        targetCompatibility = Versions.javaVersion
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    lint {
        lintConfig = File("${project.rootDir}/tools/lint/lint.xml")
        if (isEnterpriseBuild) {
            // Disable check on ObsoleteSdkInt for Enterprise builds
            // since the min sdk is higher for Enterprise builds
            disable.add("ObsoleteSdkInt")
        }
        checkDependencies = false
        abortOnError = true
        ignoreTestSources = true
        ignoreTestFixturesSources = true
        checkGeneratedSources = false
    }
}

fun CommonExtension<*, *, *, *, *, *>.composeConfig() {

    buildFeatures {
        compose = true
    }

    packaging {
        resources.excludes.apply {
            add("META-INF/AL2.0")
            add("META-INF/LGPL2.1")
        }
    }

    lint {
        // Extra rules for compose
        // Disabled until lint stops inspecting generated ksp files...
        // error.add("ComposableLambdaParameterNaming")
        error.add("ComposableLambdaParameterPosition")
        ignoreTestFixturesSources = true
        checkGeneratedSources = false
    }
}

