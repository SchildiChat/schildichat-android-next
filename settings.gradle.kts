/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

import java.net.URI

fun getLocalProperty(key: String, file: String = "local.properties"): Any? {
    val properties = java.util.Properties()
    val localProperties = File(file)
    if (localProperties.isFile) {
        java.io.InputStreamReader(java.io.FileInputStream(localProperties), Charsets.UTF_8).use { reader ->
            properties.load(reader)
        }
    } else null

    return try { properties.getProperty(key) } catch (e: Exception) { null }
}

pluginManagement {
    repositories {
        includeBuild("plugins")
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // Snapshot versions
        maven {
            url = URI("https://s01.oss.sonatype.org/content/repositories/snapshots")
            content {
                includeModule("org.matrix.rustcomponents", "sdk-android")
                includeModule("io.element.android", "wysiwyg")
                includeModule("io.element.android", "wysiwyg-compose")
            }
        }
        // To have immediate access to Rust SDK versions without a sync with Maven Central
        maven {
            url = URI("https://s01.oss.sonatype.org/content/repositories/releases")
            content {
                includeModule("org.matrix.rustcomponents", "sdk-android")
            }
        }
        google()
        mavenCentral()
        maven {
            url = URI("https://www.jitpack.io")
            content {
                includeModule("com.github.matrix-org", "matrix-analytics-events")
                includeModule("com.github.SchildiChat", "element-compound-android")
                includeModule("com.github.UnifiedPush", "android-foss_embedded_fcm_distributor")
            }
        }
        // SC forks of upstream Rust projects
        maven {
            url = URI("https://maven.pkg.github.com/SchildiChat/matrix-rust-components-kotlin")
            content {
                includeModule("chat.schildi.rustcomponents", "sdk-android")
            }
            credentials {
                username = getLocalProperty("gpr.user") as String? ?: System.getenv("GPR_USER")
                password = getLocalProperty("gpr.token") as String? ?: System.getenv("GPR_TOKEN")
            }
        }
        maven {
            url = URI("https://maven.pkg.github.com/SchildiChat/matrix-rich-text-editor")
            content {
                includeModule("chat.schildi", "wysiwyg")
                includeModule("chat.schildi", "wysiwyg-compose")
            }
            credentials {
                username = getLocalProperty("gpr.user") as String? ?: System.getenv("GPR_USER")
                password = getLocalProperty("gpr.token") as String? ?: System.getenv("GPR_TOKEN")
            }
        }
        // SC forks end
        flatDir {
            dirs("libraries/matrix/libs")
        }
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "SchildiNext"
include(":app")
include(":appnav")
include(":appconfig")
include(":appicon:element")
include(":appicon:enterprise")
include(":tests:konsist")
include(":tests:uitests")
include(":tests:testutils")
include(":anvilannotations")
include(":anvilcodegen")

fun includeProjects(directory: File, path: String, maxDepth: Int = 1) {
    directory.listFiles().orEmpty().also { it.sort() }.forEach { file ->
        if (file.isDirectory) {
            val newPath = "$path:${file.name}"
            val buildFile = File(file, "build.gradle.kts")
            if (buildFile.exists()) {
                include(newPath)
                logger.lifecycle("Included project: $newPath")
            } else if (maxDepth > 0) {
                includeProjects(file, newPath, maxDepth - 1)
            }
        }
    }
}

includeProjects(File(rootDir, "enterprise"), ":enterprise", maxDepth = 2)
includeProjects(File(rootDir, "features"), ":features")
includeProjects(File(rootDir, "libraries"), ":libraries")
includeProjects(File(rootDir, "services"), ":services")
includeProjects(File(rootDir, "schildi"), ":schildi")
