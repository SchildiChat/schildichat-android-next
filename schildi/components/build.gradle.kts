plugins {
    id("io.element.android-compose-library")
    id("kotlin-parcelize")
}

android {
    namespace = "chat.schildi.components"
}

dependencies {
    implementation(projects.schildi.lib)
    implementation(projects.libraries.designsystem)
    implementation(libs.skydoves.colorpicker)
}
