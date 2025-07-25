/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

@file:Suppress("UnstableApiUsage")

import com.android.build.api.variant.FilterConfiguration.FilterType.ABI
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.android.build.gradle.tasks.GenerateBuildConfig
import com.google.firebase.appdistribution.gradle.firebaseAppDistribution
import config.BuildTimeConfig
import extension.AssetCopyTask
import extension.ComponentMergingStrategy
import extension.GitBranchNameValueSource
import extension.GitRevisionValueSource
import extension.allEnterpriseImpl
import extension.allFeaturesImpl
import extension.allLibrariesImpl
import extension.allServicesImpl
import extension.buildConfigFieldStr
import extension.koverDependencies
import extension.locales
import extension.setupAnvil
import extension.setupKover
import java.util.Locale

plugins {
    id("io.element.android-compose-application")
    alias(libs.plugins.kotlin.android)
    // When using precompiled plugins, we need to apply the firebase plugin like this
    id(libs.plugins.firebaseAppDistribution.get().pluginId)
    alias(libs.plugins.knit)
    id("kotlin-parcelize")
    alias(libs.plugins.licensee)
    alias(libs.plugins.kotlin.serialization)
    // To be able to update the firebase.xml files, uncomment and build the project
    // id("com.google.gms.google-services")
}

setupKover()

android {
    namespace = "io.element.android.x"

    defaultConfig {
        //applicationId = BuildTimeConfig.APPLICATION_ID
        applicationId = "chat.schildi.android"
        versionCode = 1040
        versionName = "0.10.4-ex_25_7_1"
        targetSdk = Versions.TARGET_SDK

        // Keep abiFilter for the universalApk
        ndk {
            abiFilters += listOf("armeabi-v7a", "x86", "arm64-v8a", "x86_64")
        }

        // Ref: https://developer.android.com/studio/build/configure-apk-splits.html#configure-abi-split
        splits {
            // Configures multiple APKs based on ABI.
            abi {
                val buildingAppBundle = gradle.startParameter.taskNames.any { it.contains("bundle") }

                // Enables building multiple APKs per ABI. This should be disabled when building an AAB.
                isEnable = !buildingAppBundle

                // By default all ABIs are included, so use reset() and include to specify that we only
                // want APKs for armeabi-v7a, x86, arm64-v8a and x86_64.
                // Resets the list of ABIs that Gradle should create APKs for to none.
                reset()

                if (!buildingAppBundle) {
                    // Specifies a list of ABIs that Gradle should create APKs for.
                    include("armeabi-v7a", "x86", "arm64-v8a", "x86_64")
                    // Generate a universal APK that includes all ABIs, so user who installs from CI tool can use this one by default.
                    isUniversalApk = true
                }
            }
        }

        androidResources {
            localeFilters += locales
        }
    }

    signingConfigs {
        getByName("debug") {
            keyAlias = "androiddebugkey"
            keyPassword = "android"
            storeFile = file("./signature/debug.keystore")
            storePassword = "android"
        }
        register("nightly") {
            keyAlias = System.getenv("ELEMENT_ANDROID_NIGHTLY_KEYID")
                ?: project.property("signing.element.nightly.keyId") as? String?
            keyPassword = System.getenv("ELEMENT_ANDROID_NIGHTLY_KEYPASSWORD")
                ?: project.property("signing.element.nightly.keyPassword") as? String?
            storeFile = file("./signature/nightly.keystore")
            storePassword = System.getenv("ELEMENT_ANDROID_NIGHTLY_STOREPASSWORD")
                ?: project.property("signing.element.nightly.storePassword") as? String?
        }
    }

    val baseAppName = BuildTimeConfig.APPLICATION_NAME
    logger.warnInBox("Building ${defaultConfig.applicationId} ($baseAppName)")

    buildTypes {
        val oidcRedirectSchemeBase = BuildTimeConfig.METADATA_HOST_REVERSED ?: "io.element.android"
        getByName("debug") {
            resValue("string", "app_name", "$baseAppName dbg")
            resValue(
                "string",
                "login_redirect_scheme",
                "$oidcRedirectSchemeBase.debug",
            )
            applicationIdSuffix = ".debug"
            signingConfig = signingConfigs.getByName("debug")
        }

        getByName("release") {
            resValue("string", "app_name", baseAppName)
            resValue(
                "string",
                "login_redirect_scheme",
                oidcRedirectSchemeBase,
            )
            signingConfig = signingConfigs.getByName("debug")

            postprocessing {
                isRemoveUnusedCode = true
                isObfuscate = false
                isOptimizeCode = true
                isRemoveUnusedResources = true
                proguardFiles("proguard-rules.pro")
            }
        }

        register("nightly") {
            val release = getByName("release")
            initWith(release)
            applicationIdSuffix = ".nightly"
            versionNameSuffix = "-nightly"
            resValue("string", "app_name", "$baseAppName nightly")
            resValue(
                "string",
                "login_redirect_scheme",
                "$oidcRedirectSchemeBase.nightly",
            )
            matchingFallbacks += listOf("release")
            signingConfig = signingConfigs.getByName("nightly")

            postprocessing {
                initWith(release.postprocessing)
            }

            firebaseAppDistribution {
                artifactType = "APK"
                // We upload the universal APK to fix this error:
                // "App Distribution found more than 1 output file for this variant.
                // Please contact firebase-support@google.com for help using APK splits with App Distribution."
                artifactPath = "$rootDir/app/build/outputs/apk/gplay/nightly/app-gplay-universal-nightly.apk"
                // artifactType = "AAB"
                // artifactPath = "$rootDir/app/build/outputs/bundle/nightly/app-nightly.aab"
                releaseNotesFile = "tools/release/ReleaseNotesNightly.md"
                groups = if (isEnterpriseBuild) {
                    "enterprise-testers"
                } else {
                    "external-testers"
                }
                // This should not be required, but if I do not add the appId, I get this error:
                // "App Distribution halted because it had a problem uploading the APK: [404] Requested entity was not found."
                appId = if (isEnterpriseBuild) {
                    "1:912726360885:android:3f7e1fe644d99d5a00427c"
                } else {
                    "1:912726360885:android:e17435e0beb0303000427c"
                }
            }
        }
    }

    buildFeatures {
        buildConfig = true
    }
    flavorDimensions += "store"
    productFlavors {
        create("gplay") {
            dimension = "store"
            //isDefault = true
            buildConfigFieldStr("SHORT_FLAVOR_DESCRIPTION", "G")
            buildConfigFieldStr("FLAVOR_DESCRIPTION", "GooglePlay")
        }
        create("fdroid") {
            dimension = "store"
            isDefault = true // SC
            buildConfigFieldStr("SHORT_FLAVOR_DESCRIPTION", "F")
            buildConfigFieldStr("FLAVOR_DESCRIPTION", "FDroid")
        }
    }
}

// SC: downstream package name and versioning, overriding Element default config while reducing merge conflicts
val scVersionMajor = 0
val scVersionMinor = 10
val scVersionPatch = 4
// Following val is set by increment_version.sh based on the values above
val scVersionMain = "0.7.6"
android {
    // Use a flavor for common things that the upstream config will not override by the build type
    flavorDimensions += "package"
    flavorDimensions += "sc-variant"
    productFlavors {
        // Common upstream overrides across all sc variants - only one flavor for this dimension to ensure it's picked up!
        create("sc") {
            dimension = "package"
            versionCode = 1040
            versionName = "0.10.4-ex_25_7_1"
            isDefault = true
        }
        // SC variants for different release tracks. Cannot do actual release types for those since fdroid build tools always want `release` builds.
        create("default") {
            dimension = "sc-variant"
            applicationId = "chat.schildi.android"
            isDefault = true
            resValue("string", "sc_app_name", "SchildiChat Next")
            resValue("string", "sc_app_name_launcher", "SchildiNext")
        }
        create("beta") {
            dimension = "sc-variant"
            applicationId = "chat.schildi.next"
            resValue("string", "sc_app_name", "SchildiChat Next (Beta)")
            resValue("string", "sc_app_name_launcher", "SchildiNext β")
        }
        create("internal") {
            dimension = "sc-variant"
            applicationId = "chat.schildi.next.internal"
            resValue("string", "sc_app_name", "SchildiChat Next (Internal)")
            resValue("string", "sc_app_name_launcher", "SchildiNext[i]")
        }
    }
    // Build types to override some more upstream values
    buildTypes {
        named("debug") {
            resValue("string", "app_name", "SchildiChat Next dbg")
            buildConfigField("String", "SC_VERSION_MAIN", "\"$scVersionMain\"")
            buildConfigField("String", "SC_VERSION_ELEMENT", "\"${Versions.VERSION_NAME}\"")
        }
        named("release") {
            resValue("string", "app_name", "SchildiChat Next")
            buildConfigField("String", "SC_VERSION_MAIN", "\"$scVersionMain\"")
            buildConfigField("String", "SC_VERSION_ELEMENT", "\"${Versions.VERSION_NAME}\"")
        }
    }
}
// SC: Disable baseline profiles to fix reproducible builds - https://github.com/SchildiChat/schildichat-android-next/issues/65
tasks.whenTaskAdded {
    if (name.contains("ArtProfile")) {
        enabled = false
    }
}
// SC: Disable unused upstream configs
androidComponents {
    beforeVariants { variantBuilder ->
        if (variantBuilder.buildType in listOf("nightly") || variantBuilder.flavorName?.startsWith("gplay") == true) {
            variantBuilder.enable = false
        }
    }
}

androidComponents {
    // map for the version codes last digit
    // x86 must have greater values than arm
    // 64 bits have greater value than 32 bits
    val abiVersionCodes = mapOf(
        "armeabi-v7a" to 1,
        "arm64-v8a" to 2,
        "x86" to 3,
        "x86_64" to 4,
    )

    onVariants { variant ->
        // Assigns a different version code for each output APK
        // other than the universal APK.
        variant.outputs.forEach { output ->
            val name = output.filters.find { it.filterType == ABI }?.identifier

            // Stores the value of abiCodes that is associated with the ABI for this variant.
            val abiCode = abiVersionCodes[name] ?: 0
            // Assigns the new version code to output.versionCode, which changes the version code
            // for only the output APK, not for the variant itself.
            output.versionCode.set((output.versionCode.orNull ?: 0) * 10 + abiCode)
        }
    }

    val reportingExtension: ReportingExtension = project.extensions.getByType(ReportingExtension::class.java)
    configureLicensesTasks(reportingExtension)
}

// Knit
apply {
    plugin("kotlinx-knit")
}

knit {
    files = fileTree(project.rootDir) {
        include(
            "**/*.md",
            "**/*.kt",
            "*/*.kts",
        )
        exclude(
            "**/build/**",
            "*/.gradle/**",
            "**/CHANGES.md",
        )
    }
}

setupAnvil(
    generateDaggerCode = true,
    generateDaggerFactoriesUsingAnvil = false,
    componentMergingStrategy = ComponentMergingStrategy.KSP,
)

dependencies {
    implementation(projects.schildi.theme)
    implementation(projects.schildi.lib) // Needed for DI
    implementation(projects.schildi.matrixsdk) // Needed for DI
    implementation(libs.androidx.work) // Still SC
    implementation(libs.androidx.emoji2)
    implementation(libs.androidx.emoji2.bundled)
    allLibrariesImpl()
    allServicesImpl()
    if (isEnterpriseBuild) {
        allEnterpriseImpl(project)
        implementation(projects.appicon.enterprise)
    } else {
        implementation(projects.appicon.element)
    }
    allFeaturesImpl(project)
    implementation(projects.features.migration.api)
    implementation(projects.appnav)
    implementation(projects.appconfig)
    implementation(projects.libraries.uiStrings)
    implementation(projects.services.analytics.compose)

    if (ModulesConfig.pushProvidersConfig.includeFirebase) {
        "gplayImplementation"(projects.libraries.pushproviders.firebase)
    }
    if (ModulesConfig.pushProvidersConfig.includeUnifiedPush) {
        implementation(projects.libraries.pushproviders.unifiedpush)
    }

    implementation(libs.appyx.core)
    implementation(libs.androidx.splash)
    implementation(libs.androidx.core)
    implementation(libs.androidx.corektx)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.startup)
    implementation(libs.androidx.preference)
    implementation(libs.coil)

    implementation(platform(libs.network.okhttp.bom))
    implementation(libs.network.okhttp.logging)
    implementation(libs.serialization.json)

    implementation(libs.matrix.emojibase.bindings)

    testImplementation(libs.test.junit)
    testImplementation(libs.test.robolectric)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.molecule.runtime)
    testImplementation(libs.test.truth)
    testImplementation(libs.test.turbine)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.services.toolbox.test)

    koverDependencies()
}

tasks.withType<GenerateBuildConfig>().configureEach {
    outputs.upToDateWhen { false }
    val gitRevision = providers.of(GitRevisionValueSource::class.java) {}.get()
    val gitBranchName = providers.of(GitBranchNameValueSource::class.java) {}.get()
    android.defaultConfig.buildConfigFieldStr("GIT_REVISION", gitRevision)
    android.defaultConfig.buildConfigFieldStr("GIT_BRANCH_NAME", gitBranchName)
}

licensee {
    ignoreDependencies("chat.schildi.rustcomponents", "sdk-android")
    ignoreDependencies("com.github.SchildiChat", "element-compound-android")
    ignoreDependencies(groupId = "chat.schildi")
    allow("Apache-2.0")
    allow("MIT")
    allow("BSD-2-Clause")
    allow("BSD-3-Clause")
    allowUrl("https://opensource.org/licenses/MIT")
    allowUrl("https://developer.android.com/studio/terms.html")
    allowUrl("https://www.zetetic.net/sqlcipher/license/")
    allowUrl("https://jsoup.org/license")
    allowUrl("https://asm.ow2.io/license.html")
    allowUrl("https://www.gnu.org/licenses/agpl-3.0.txt")
    ignoreDependencies("com.github.matrix-org", "matrix-analytics-events")
    // Ignore dependency that are not third-party licenses to us.
    ignoreDependencies(groupId = "io.element.android")
}

fun Project.configureLicensesTasks(reportingExtension: ReportingExtension) {
    androidComponents {
        onVariants { variant ->
            val capitalizedVariantName = variant.name.replaceFirstChar {
                if (it.isLowerCase()) {
                    it.titlecase(Locale.getDefault())
                } else {
                    it.toString()
                }
            }
            val artifactsFile = reportingExtension.file("licensee/android$capitalizedVariantName/artifacts.json")

            val copyArtifactsTask =
                project.tasks.register<AssetCopyTask>("copy${capitalizedVariantName}LicenseeReportToAssets") {
                    inputFile.set(artifactsFile)
                    targetFileName.set("licensee-artifacts.json")
                }
            variant.sources.assets?.addGeneratedSourceDirectory(
                copyArtifactsTask,
                AssetCopyTask::outputDirectory,
            )
            copyArtifactsTask.dependsOn("licenseeAndroid$capitalizedVariantName")
        }
    }
}

configurations.all {
    resolutionStrategy {
        dependencySubstitution {
            val tink = libs.google.tink.get()
            substitute(module("com.google.crypto.tink:tink")).using(module("${tink.group}:${tink.name}:${tink.version}"))
        }
    }
}
