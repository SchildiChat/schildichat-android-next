import extension.setupDependencyInjection

plugins {
    id("io.element.android-compose-library")
    id("kotlin-parcelize")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "chat.schildi.lib"

    buildFeatures {
        buildConfig = true
    }
}

setupDependencyInjection()

dependencies {
    implementation(projects.libraries.di)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.serialization.json)
    implementation(projects.libraries.uiStrings)
}
