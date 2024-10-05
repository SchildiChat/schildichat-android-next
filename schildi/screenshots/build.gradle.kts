import extension.allFeaturesImpl
import extension.allLibrariesImpl
import extension.allServicesImpl

plugins {
    id("io.element.android-compose-library")
    alias(libs.plugins.anvil)
    alias(libs.plugins.ksp)
}

android {
    namespace = "chat.schildi.screenshots"
}

anvil {
    generateDaggerFactories.set(true)
}

dependencies {
    implementation(projects.schildi.theme)
    allLibrariesImpl()
    allServicesImpl()
    allFeaturesImpl(project)
    implementation(projects.features.call)
    implementation(projects.anvilannotations)
    implementation(projects.appnav)
    implementation(projects.appconfig)
    anvil(projects.anvilcodegen)

    implementation(libs.appyx.core)
    implementation(libs.androidx.splash)
    implementation(libs.androidx.core)
    implementation(libs.androidx.corektx)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.startup)
    implementation(libs.androidx.preference)
    implementation(libs.coil)
    implementation(libs.coil.compose)
    implementation(libs.coil.test)
    implementation(libs.jsoup)

    implementation(platform(libs.network.okhttp.bom))
    implementation(libs.network.okhttp.logging)
    implementation(libs.serialization.json)

    implementation(libs.matrix.emojibase.bindings)

    implementation(libs.dagger)
    //kapt(libs.dagger.compiler)

    testImplementation(libs.test.junit)
    testImplementation(libs.test.robolectric)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.molecule.runtime)
    testImplementation(libs.test.truth)
    testImplementation(libs.test.turbine)
    testImplementation(projects.libraries.matrix.test)

    ksp(libs.showkase.processor)
    //koverDependencies()

    implementation(projects.libraries.testtags)
}
