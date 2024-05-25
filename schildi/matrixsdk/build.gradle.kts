plugins {
    id("io.element.android-compose-library")
    alias(libs.plugins.ksp)
    id("kotlin-parcelize")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "chat.schildi.matrixsdk"
}

dependencies {
    implementation(libs.serialization.json)
    implementation(libs.androidx.emoji2)
}
