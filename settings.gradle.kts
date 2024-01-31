import java.net.URI

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
        google()
        mavenCentral()
        maven { url = URI("https://oss.sonatype.org/content/repositories/snapshots/") }
        maven {
            url = URI("https://www.jitpack.io")
            content {
                includeModule("com.github.UnifiedPush", "android-connector")
                includeModule("com.github.UnifiedPush", "android-foss_embedded_fcm_distributor")
                includeModule("com.github.matrix-org", "matrix-analytics-events")
                includeModule("com.github.SchildiChat", "element-compound-android")
            }
        }
        // To have immediate access to Rust SDK versions
        maven {
            url = URI("https://s01.oss.sonatype.org/content/repositories/releases")
        }
        // SC fork of the Rust-SDK
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
include(":tests:konsist")
include(":tests:uitests")
include(":tests:testutils")
include(":anvilannotations")
include(":anvilcodegen")

//include(":samples:minimal")

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

includeProjects(File(rootDir, "features"), ":features")
includeProjects(File(rootDir, "libraries"), ":libraries")
includeProjects(File(rootDir, "services"), ":services")
includeProjects(File(rootDir, "schildi"), ":schildi")
