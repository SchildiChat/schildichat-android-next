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

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import chat.schildi.components.preferences.AutoRendered
import chat.schildi.components.preferences.Rendered
import chat.schildi.lib.preferences.AbstractScPref
import chat.schildi.lib.preferences.LocalScPreferencesStore
import chat.schildi.lib.preferences.ScActionablePref
import chat.schildi.lib.preferences.ScDisclaimerPref
import chat.schildi.lib.preferences.ScPrefCategory
import chat.schildi.lib.preferences.ScPref
import chat.schildi.lib.preferences.ScPrefCategoryCollapsed
import chat.schildi.lib.preferences.ScPrefCollection
import chat.schildi.lib.preferences.ScPrefContainer
import chat.schildi.lib.preferences.ScPrefScreen
import chat.schildi.lib.preferences.ScPrefs
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.components.preferences.PreferencePage
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.theme.badgeNegativeContentColor
import io.element.android.libraries.designsystem.theme.components.Surface
import timber.log.Timber

@Composable
fun ScTweaksSettingsView(
    state: ScTweaksSettingsState,
    onBackClick: () -> Unit,
    onOpenPrefScreen: (ScPrefScreen) -> Unit,
    handleScPrefAction: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val showPushInfoDialog = remember { mutableStateOf(false) }
    PushInfoDialog(state.pushInfo, showPushInfoDialog)
    val showUserChangedSettings = remember { mutableStateOf(false) }
    UserChangedSettingsDialog(showUserChangedSettings)
    PreferencePage(
        modifier = modifier,
        onBackClick = onBackClick,
        title = stringResource(id = state.titleRes)
    ) {
        RecursiveScPrefsView(
            state = state,
            prefs = state.scPrefs,
            onOpenPrefScreen = onOpenPrefScreen,
            handleScPrefAction = { key ->
                when (key) {
                    ScPrefs.SC_PUSH_INFO.key -> showPushInfoDialog.value = true
                    ScPrefs.SC_USER_CHANGED_SETTINGS.key -> showUserChangedSettings.value = true
                    else -> handleScPrefAction(key)
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
            is ScPrefCategoryCollapsed -> {
                val prefVal = state.prefVals[scPref.sKey]
                if (prefVal == null) {
                    Timber.e("Missing value for ${scPref.sKey}")
                    return@forEach
                }
                scPref.Rendered(
                    initial = prefVal,
                    onChange = { newVal -> state.eventSink(ScTweaksSettingsEvents.SetScPref(scPref, newVal)) }
                ) {
                    RecursiveScPrefsView(
                        state = state,
                        prefs = scPref.prefs,
                        onOpenPrefScreen = onOpenPrefScreen,
                        handleScPrefAction = handleScPrefAction,
                    )
                }
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
            is ScPrefCollection -> {
                RecursiveScPrefsView(
                    state = state,
                    prefs = scPref.prefs,
                    onOpenPrefScreen = onOpenPrefScreen,
                    handleScPrefAction = handleScPrefAction,
                )
            }
            is ScPrefScreen -> {
                scPref.Rendered {
                    onOpenPrefScreen(scPref)
                }
            }
            is ScActionablePref -> {
                scPref.Rendered(handleAction = handleScPrefAction)
            }
            is ScDisclaimerPref -> {
                scPref.Rendered()
            }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserChangedSettingsDialog(show: MutableState<Boolean>) {
    if (show.value) {
        BasicAlertDialog(onDismissRequest = { show.value = false }) {
            Surface(shape = RoundedCornerShape(12.dp)) {
                val context = LocalContext.current
                val categoryColor = MaterialTheme.colorScheme.onSurfaceVariant
                val prefColor = MaterialTheme.colorScheme.onSurface
                val prefEnabledColor = ElementTheme.colors.bgAccentPressed
                val prefDisabledColor = ElementTheme.colors.badgeNegativeContentColor
                val changedPrefs = LocalScPreferencesStore.current.combinedSettingValueAndEnabledFlow { getValue, isEnabled ->
                    buildAnnotatedString {
                        buildNestedSettingsString(
                            context,
                            filterUserChangedSettings(getValue = getValue, isEnabled = isEnabled),
                            getValue = getValue,
                            categoryColor = categoryColor,
                            enabledPrefColor = prefEnabledColor,
                            disabledPrefColor = prefDisabledColor,
                            otherPrefColor = prefColor,
                        )
                    }
                }.collectAsState(AnnotatedString("...")).value
                Text(
                    changedPrefs,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
                )
            }
        }
    }
}

private fun AnnotatedString.Builder.buildNestedSettingsString(
    context: Context,
    settings: List<AbstractScPref>,
    getValue: (ScPref<*>) -> Any?,
    categoryColor: Color,
    enabledPrefColor: Color,
    disabledPrefColor: Color,
    otherPrefColor: Color,
    depth: Int = 0
) {
    if (settings.isEmpty()) {
        append(context.getString(chat.schildi.lib.R.string.sc_pref_user_changed_prefs_empty))
        return
    }
    settings.forEach {
        for (i in 0 until depth - (if (it is ScPref<*>) 1 else 0)) {
            append("  ")
        }
        if (it is ScPref<*>) {
            val v = getValue(it)
            val color = when (v) {
                true -> enabledPrefColor
                false -> disabledPrefColor
                else -> otherPrefColor
            }
            append("- ")
            pushStyle(SpanStyle(color = color))
            append(context.getString(it.titleRes))
            pop()
        } else {
            pushStyle(SpanStyle(color = categoryColor))
            append("[")
            append(context.getString(it.titleRes))
            append("]")
            pop()
        }
        append("\n")
        if (it is ScPrefContainer) {
            buildNestedSettingsString(context, it.prefs, getValue, categoryColor, enabledPrefColor, disabledPrefColor, otherPrefColor, depth = depth + 1)
        }
    }
}

private fun filterUserChangedSettings(
    settings: List<AbstractScPref> = ScPrefs.scTweaks.prefs,
    getValue: (ScPref<*>) -> Any?,
    isEnabled: (ScPref<*>) -> Boolean,
): List<AbstractScPref> {
    return settings.mapNotNull {
        if (it is ScPrefContainer) {
            val filteredChildren = filterUserChangedSettings(it.prefs, getValue, isEnabled)
            if (filteredChildren.isEmpty()) {
                null
            } else {
                ScPrefCategory(
                    titleRes = it.titleRes,
                    summaryRes = it.summaryRes,
                    prefs = filteredChildren,
                )
            }
        } else if (it is ScPref<*>) {
            if (!isEnabled(it)) {
                null
            } else if (it.defaultValue == getValue(it)) {
                null
            } else {
                it
            }
        } else {
            null
        }
    }
}

@PreviewsDayNight
@Composable
internal fun ScTweaksSettingsViewPreview(@PreviewParameter(ScTweaksSettingsStateProvider::class) state: ScTweaksSettingsState) =
    ElementPreview {
        ScTweaksSettingsView(state = state, onBackClick = {}, onOpenPrefScreen = {}, handleScPrefAction = {})
    }
