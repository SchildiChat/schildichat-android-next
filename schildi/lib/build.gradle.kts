import extension.setupAnvil

plugins {
    id("io.element.android-compose-library")
    id("kotlin-parcelize")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "chat.schildi.lib"
}

setupAnvil()

dependencies {
    implementation(projects.libraries.di)
    implementation(libs.dagger)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.serialization.json)
    implementation(projects.libraries.uiStrings)
}
