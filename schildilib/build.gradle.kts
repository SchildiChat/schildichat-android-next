plugins {
    id("io.element.android-compose-library")
    id("kotlin-parcelize")
}

android {
    namespace = "chat.schildi.lib"
}

dependencies {
    implementation(libs.androidx.datastore.preferences)
}
