plugins {
    id("io.element.android-compose-library")
    id("kotlin-parcelize")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.anvil)
}

android {
    namespace = "chat.schildi.lib"
}

anvil {
    generateDaggerFactories.set(true)
}

dependencies {
    anvil(projects.anvilcodegen)
    implementation(projects.anvilannotations)
    implementation(projects.libraries.di)
    implementation(libs.dagger)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.serialization.json)
    implementation(projects.libraries.uiStrings)

    api(projects.libraries.featureflag.api)
}
