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
import chat.schildi.lib.preferences.ScPref
import chat.schildi.lib.preferences.ScPrefContainer
import chat.schildi.lib.preferences.ScPrefScreen
import chat.schildi.lib.preferences.ScPreferencesStore
import chat.schildi.lib.preferences.ScPrefs
import chat.schildi.lib.preferences.forEachPreference
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.Inject
import io.element.android.annotations.ContributesNode
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

@ContributesNode(SessionScope::class)
@Inject
class ScTweaksSettingsNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: ScTweaksSettingsPresenter.Factory,
    private val appPreferencesStore: AppPreferencesStore,
    private val scPreferencesStore: ScPreferencesStore,
    @SessionCoroutineScope
    private val sessionCoroutineScope: CoroutineScope,
) : Node(buildContext, plugins = plugins) {

    data class Inputs(
        val prefScreen: ScPrefScreen?
    ) : NodeInputs

    interface Callback : Plugin {
        fun onOpenScTweaks(scPrefScreen: ScPrefScreen)
    }

    private val prefScreen = inputs<Inputs>().prefScreen
    val presenter = presenterFactory.create(prefScreen)

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        ScTweaksSettingsView(
            state = state,
            modifier = modifier,
            onBackClick = ::navigateUp,
            onOpenPrefScreen = this::onOpenScTweaks,
            handleScPrefAction = this::handleScPrefAction,
        )
    }

    private fun onOpenScTweaks(scPrefScreen: ScPrefScreen) {
        plugins<Callback>().forEach {
            it.onOpenScTweaks(scPrefScreen)
        }
    }

    private fun handleScPrefAction(key: String) {
        when (key) {
            ScPrefs.SC_RESTORE_DEFAULTS.key -> {
                batchSetScPrefs { it.defaultValue }
                setUpstreamSettingsToPreset(false)
            }
            ScPrefs.SC_RESTORE_UPSTREAM.key -> {
                batchSetScPrefs { it.upstreamChoice }
            }
            ScPrefs.SC_RESTORE_AUTHORS_CHOICE.key -> {
                batchSetScPrefs { it.authorsChoice ?: it.defaultValue }
                setUpstreamSettingsToPreset(true)
            }
            ScPrefs.SC_RESTORE_ADVANCED_THEME_DEFAULTS.key -> {
                batchSetScPrefs(ScPrefs.scTweaksAdvancedTheming) { it.defaultValue }
                setUpstreamSettingsToPreset(false)
            }
            else -> Timber.e("Unhandled actionable pref $key")
        }
    }

    private fun batchSetScPrefs(parent: ScPrefContainer = ScPrefs.scTweaks, to: (ScPref<*>) -> Any?) {
        parent.forEachPreference { pref ->
            if (pref.sKey in ScPrefs.prefsToExcludeFromBatchSet) {
                return@forEachPreference
            }
            sessionCoroutineScope.launch {
                to(pref)?.let {
                    scPreferencesStore.setSettingTypesafe(pref, it)
                }
            }
        }
    }

    private fun setUpstreamSettingsToPreset(authorsChoice: Boolean) = sessionCoroutineScope.launch {
        // "View source" setting
        appPreferencesStore.setDeveloperModeEnabled(authorsChoice)
    }
}
