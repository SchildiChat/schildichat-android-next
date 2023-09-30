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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.libraries.designsystem.components.preferences.PreferenceView
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.ui.strings.CommonStrings
import timber.log.Timber

@Composable
fun ScTweaksSettingsView(
    state: ScTweaksSettingsState,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PreferenceView(
        modifier = modifier,
        onBackPressed = onBackPressed,
        title = stringResource(id = CommonStrings.common_advanced_settings)
    ) {
        Timber.e("SC_TWEAKS REDRAW") // TODO remove, just for debugging
        state.scPrefs.forEach { scPref ->
            val prefVal = state.prefVals[scPref.sKey]
            if (prefVal == null) {
                Timber.e("Missing value for ${scPref.sKey}")
                return@forEach
            }

            scPref.Rendered(
                initial = prefVal,
                onChange = { newVal -> state.eventSink(ScTweaksSettingsEvents.SetScPref(scPref, newVal)) }
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun ScTweaksSettingsViewPreview(@PreviewParameter(ScTweaksSettingsStateProvider::class) state: ScTweaksSettingsState) =
    ElementPreview {
        ScTweaksSettingsView(state = state, onBackPressed = { })
    }
