import extension.setupAnvil

plugins {
    id("io.element.android-compose-library")
    alias(libs.plugins.ksp)
    id("kotlin-parcelize")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "chat.schildi.matrixsdk"
}

setupAnvil()

dependencies {
    implementation(projects.libraries.di)
    implementation(libs.dagger)
    implementation(libs.serialization.json)
    implementation(libs.androidx.emoji2)
    implementation(project(":libraries:matrix:api"))
    implementation(project(":anvilannotations"))
}
