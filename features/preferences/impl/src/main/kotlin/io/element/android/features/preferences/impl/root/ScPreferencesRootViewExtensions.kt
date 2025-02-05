package io.element.android.features.preferences.impl.root

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.core.meta.isGplayBuild
import io.element.android.libraries.matrix.api.core.DeviceId
import io.element.android.libraries.ui.strings.CommonStrings

private fun String.removeUpstreamVersionString(): String {
    val index = indexOf("-ex_")
    return if (index > 0) {
        substring(0, index)
    } else {
        this
    }
}

private fun buildVersionString(context: Context, buildMeta: BuildMeta, deviceId: DeviceId?) = buildString {
    // Main SC version & variant
    append(
        context.getString(
            CommonStrings.settings_version_number,
            buildMeta.versionName.removeUpstreamVersionString(),
            buildMeta.scBuildMeta.scVariant?.let {
                "${buildMeta.versionCode}, $it"
            } ?: buildMeta.versionCode.toString()
        )
    )
    // Element version
    append("\n")
    append(
        context.getString(
            chat.schildi.lib.R.string.sc_about_based_on_element_x_version,
            buildMeta.scBuildMeta.elementVersion,
        )
    )
    // Git revision
    if (!buildMeta.isGplayBuild) {
        append("\n")
        append(context.getString(chat.schildi.lib.R.string.sc_about_git_revision, buildMeta.gitRevision))
    }
    // Device ID
    if (deviceId != null) {
        append("\n")
        append(context.getString(chat.schildi.lib.R.string.sc_about_device_id, deviceId))
    }
}

@Composable
internal fun buildScVersionString(
    buildMeta: BuildMeta?,
    deviceId: DeviceId?,
): String? {
    buildMeta ?: return null
    val context = LocalContext.current
    return remember(buildMeta, deviceId) { buildVersionString(context, buildMeta, deviceId) }
}
