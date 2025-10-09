plugins {
    id("io.element.android-compose-library")
    alias(libs.plugins.ksp)
}

android {
    namespace = "chat.schildi.theme"
}

dependencies {
    implementation(projects.schildi.lib)
    implementation(libs.androidx.compose.material3)
    implementation(projects.libraries.compound)
}
