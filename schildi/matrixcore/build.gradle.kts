import extension.setupDependencyInjection

plugins {
    id("io.element.android-library")
    alias(libs.plugins.ksp)
}

android {
    namespace = "chat.schildi.matrixcore"
}

dependencies {
}
