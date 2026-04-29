package io.element.android.features.login.impl.classic

import android.content.Context
import android.content.pm.PackageManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.di.annotations.ApplicationContext

fun interface ScLegacyPackageProvider {
    fun get(): String
}

@ContributesBinding(AppScope::class)
class DefaultScLegacyPackageProvider(
    @ApplicationContext private val context: Context,
) : ScLegacyPackageProvider {
    override fun get(): String {
        return packageNames.firstOrNull(::isInstalled) ?: packageNames.first()
    }

    private fun isInstalled(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    private companion object {
        val packageNames = listOf(
            "de.spiritcroc.riotx",
            "de.spiritcroc.riotx.fcm",
            "de.spiritcroc.riotx.foss",
            "de.spiritcroc.riotx.testing.fcm",
            "de.spiritcroc.riotx.testing.foss",
            "de.spiritcroc.riotx.debug",
        )
    }
}
