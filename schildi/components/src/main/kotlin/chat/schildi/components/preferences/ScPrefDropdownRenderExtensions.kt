package chat.schildi.components.preferences

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import chat.schildi.lib.preferences.AbstractScPref
import chat.schildi.lib.preferences.LocalScPreferencesStore
import chat.schildi.lib.preferences.ScBoolPref
import chat.schildi.lib.preferences.ScPref
import chat.schildi.lib.preferences.ScPrefContainer
import chat.schildi.lib.preferences.enabledState
import chat.schildi.lib.preferences.settingState
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.theme.components.Checkbox
import io.element.android.libraries.designsystem.theme.components.DropdownMenu
import io.element.android.libraries.designsystem.theme.components.DropdownMenuItem
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
fun AbstractScPref.AutoRenderedDropdown(
    onClick: () -> Unit,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    when (this) {
        is ScBoolPref -> return RenderedDropdown(leadingIcon)
        //is ScUpstreamFeatureFlagAliasPref -> return RenderedDropdown(leadingIcon)
        is ScPrefContainer -> return RenderedDropdown(onClick, leadingIcon)
        else -> {
            Timber.w("Not supported to render ScPref ${this.javaClass} as option")
        }
    }
}

@Composable
fun ScPrefContainer.RenderedDropdown(
    onClick: () -> Unit,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    var showMenu by remember { mutableStateOf(false) }

    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = { showMenu = false }
    ) {
        prefs.forEach {
            it.AutoRenderedDropdown(
                onClick = {
                    showMenu = false
                    onClick()
                }
            )
        }
    }

    val enabled = LocalScPreferencesStore.current.enabledState(this).value
    DropdownMenuItem(
        enabled = enabled,
        onClick = {
            if (!enabled) return@DropdownMenuItem
            showMenu = true
        },
        text = { Text(stringResource(id = titleRes)) },
        leadingIcon = leadingIcon,
        trailingIcon = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowRight,
                tint = ElementTheme.materialColors.secondary,
                contentDescription = null,
            )
        }
    )
}

@Composable
fun ScPref<Boolean>.RenderedDropdown(
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    val scPrefs = LocalScPreferencesStore.current
    val enabled = scPrefs.enabledState(this).value
    val pref = this
    val coroutineScope = rememberCoroutineScope()
    val currentValue = scPrefs.settingState(this).value
    DropdownMenuItem(
        enabled = enabled,
        onClick = {
            if (!enabled) return@DropdownMenuItem
            coroutineScope.launch {
                scPrefs.setSetting(pref, !currentValue)
            }
        },
        leadingIcon = leadingIcon,
        text = {
            Text(
                stringResource(id = titleRes),
                maxLines = 1
            )
        },
        trailingIcon = {
            Checkbox(
                checked = currentValue,
                enabled = enabled,
                onCheckedChange = {
                    if (!enabled) return@Checkbox
                    coroutineScope.launch {
                        scPrefs.setSetting(pref, it)
                    }
                },
                modifier = Modifier.size(18.dp),
            )
        }
    )
}

/*
@Composable
fun ScUpstreamFeatureFlagAliasPref.RenderedDropdown(
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    val coroutineScope = rememberCoroutineScope()
    val featureFlagService = LocalScPreferencesStore.current.featureFlagService
    if (featureFlagService == null) {
        Timber.w("Cannot render ScUpstreamFeatureFlagAliasPref without featureFlagService")
        return
    }
    val currentValue = featureFlagService.isFeatureEnabledFlow(featureFlag).collectAsState(/*TODO buildMeta.*/defaultValue).value
    DropdownMenuItem(
        onClick = {
            coroutineScope.launch {
                featureFlagService.setFeatureEnabled(featureFlag, !currentValue)
            }
        },
        leadingIcon = leadingIcon,
        text = {
            Text(
                stringResource(id = titleRes),
                maxLines = 1
            )
        },
        trailingIcon = {
            Checkbox(
                checked = currentValue,
                onCheckedChange = {
                    coroutineScope.launch {
                        featureFlagService.setFeatureEnabled(featureFlag, it)
                    }
                },
                modifier = Modifier.size(18.dp),
            )
        }
    )
}
 */
