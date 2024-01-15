plugins {
    id("io.element.android-compose-library")
    id("kotlin-parcelize")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "chat.schildi.lib"
}

dependencies {
    implementation(libs.androidx.datastore.preferences)
    implementation(projects.libraries.uiStrings)
    implementation(libs.serialization.json)
}
