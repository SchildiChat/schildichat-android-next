/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package chat.schildi.preferences.tweaks

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import chat.schildi.components.preferences.AutoRendered
import chat.schildi.components.preferences.Rendered
import chat.schildi.lib.preferences.AbstractScPref
import chat.schildi.lib.preferences.ScActionablePref
import chat.schildi.lib.preferences.ScPrefCategory
import chat.schildi.lib.preferences.ScPref
import chat.schildi.lib.preferences.ScPrefScreen
import chat.schildi.lib.preferences.ScPrefs
import io.element.android.libraries.designsystem.components.preferences.PreferencePage
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.theme.components.Surface
import timber.log.Timber

@Composable
fun ScTweaksSettingsView(
    state: ScTweaksSettingsState,
    onBackPressed: () -> Unit,
    onOpenPrefScreen: (ScPrefScreen) -> Unit,
    handleScPrefAction: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showPushInfoDialog = remember { mutableStateOf(false) }
    PushInfoDialog(state.pushInfo, showPushInfoDialog)
    PreferencePage(
        modifier = modifier,
        onBackPressed = onBackPressed,
        title = stringResource(id = state.titleRes)
    ) {
        RecursiveScPrefsView(
            state = state,
            prefs = state.scPrefs,
            onOpenPrefScreen = onOpenPrefScreen,
            handleScPrefAction = { key ->
                if (key == ScPrefs.SC_PUSH_INFO.key) {
                    showPushInfoDialog.value = true
                } else {
                    handleScPrefAction(key)
                }
            },
        )
    }
}

@Composable
fun RecursiveScPrefsView(
    state: ScTweaksSettingsState,
    prefs: List<AbstractScPref>,
    onOpenPrefScreen: (ScPrefScreen) -> Unit,
    handleScPrefAction: (String) -> Unit,
) {
    prefs.forEach { scPref ->
        when (scPref) {
            is ScPref<*> -> {
                val prefVal = state.prefVals[scPref.sKey]
                if (prefVal == null) {
                    Timber.e("Missing value for ${scPref.sKey}")
                    return@forEach
                }

                scPref.AutoRendered(
                    initial = prefVal,
                    onChange = { newVal -> state.eventSink(ScTweaksSettingsEvents.SetScPref(scPref, newVal)) }
                )
            }
            is ScPrefCategory -> {
                scPref.Rendered {
                    RecursiveScPrefsView(
                        state = state,
                        prefs = scPref.prefs,
                        onOpenPrefScreen = onOpenPrefScreen,
                        handleScPrefAction = handleScPrefAction,
                    )
                }
            }
            is ScPrefScreen -> {
                scPref.Rendered {
                    onOpenPrefScreen(scPref)
                }
            }
            is ScActionablePref -> {
                scPref.Rendered(handleAction = handleScPrefAction)
            }
            else -> Timber.e("Unhandled ScPref type ${scPref.javaClass.simpleName}")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PushInfoDialog(pushInfo: String, show: MutableState<Boolean>) {
    if (show.value) {
        BasicAlertDialog(onDismissRequest = { show.value = false }) {
            Surface(shape = RoundedCornerShape(12.dp)) {
                Text(
                    pushInfo,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun ScTweaksSettingsViewPreview(@PreviewParameter(ScTweaksSettingsStateProvider::class) state: ScTweaksSettingsState) =
    ElementPreview {
        ScTweaksSettingsView(state = state, onBackPressed = {}, onOpenPrefScreen = {}, handleScPrefAction = {})
    }
