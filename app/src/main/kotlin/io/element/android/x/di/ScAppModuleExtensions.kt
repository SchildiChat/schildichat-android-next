package io.element.android.x.di

import io.element.android.libraries.core.meta.ScBuildMeta
import io.element.android.x.BuildConfig

fun createScBuildMeta() = ScBuildMeta(
    mainVersion = BuildConfig.SC_VERSION_MAIN,
    elementVersion = BuildConfig.SC_VERSION_ELEMENT,
    scVariant = BuildConfig.FLAVOR_sc_variant.takeIf { it != "default" },
)
