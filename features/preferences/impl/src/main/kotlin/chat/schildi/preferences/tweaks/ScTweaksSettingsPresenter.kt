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
import androidx.compose.runtime.rememberCoroutineScope
import chat.schildi.lib.preferences.ScPrefScreen
import chat.schildi.lib.preferences.ScPrefs
import chat.schildi.lib.preferences.collectScPrefs
import chat.schildi.lib.preferences.prefValMap
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.element.android.features.preferences.api.store.PreferencesStore
import io.element.android.libraries.architecture.Presenter
import kotlinx.coroutines.launch
import timber.log.Timber

class ScTweaksSettingsPresenter @AssistedInject constructor(
    preferencesStore: PreferencesStore,
    @Assisted prefScreen: ScPrefScreen?,
) : Presenter<ScTweaksSettingsState> {

    private val scPreferencesStore = preferencesStore.getScPreferenceStore()
    private val prefScreen = prefScreen ?: ScPrefs.scTweaks

    @AssistedFactory
    interface Factory {
        fun create(prefScreen: ScPrefScreen?): ScTweaksSettingsPresenter
    }

    @Composable
    override fun present(): ScTweaksSettingsState {
        val localCoroutineScope = rememberCoroutineScope()

        val prefVals = prefScreen.prefs.collectScPrefs().prefValMap {
            scPreferencesStore.settingState(scPref = it).value
        }

        fun handleEvents(event: ScTweaksSettingsEvents) {
            when (event) {
                is ScTweaksSettingsEvents.SetScPref<*> -> localCoroutineScope.launch {
                    Timber.v("Handle SC tweak: ${event.scPref.key} -> ${event.value}")
                    scPreferencesStore.setSettingTypesafe(event.scPref, event.value)
                }
            }
        }

        return ScTweaksSettingsState(
            titleRes = prefScreen.titleRes,
            scPrefs = prefScreen.prefs,
            prefVals = prefVals,
            eventSink = ::handleEvents
        )
    }
}
