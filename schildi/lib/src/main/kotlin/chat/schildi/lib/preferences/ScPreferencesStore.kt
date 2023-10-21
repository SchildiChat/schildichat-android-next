package chat.schildi.lib.preferences

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import chat.schildi.lib.R
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "schildinext-preferences")

object ScPrefs {

    // Appearance
    val SC_THEME = ScBoolPref("SC_THEMES", true, R.string.sc_pref_sc_themes_title, upstreamChoice = false)
    val EL_TYPOGRAPHY = ScBoolPref("EL_TYPOGRAPHY", false, R.string.sc_pref_el_typography_title, R.string.sc_pref_el_typography_summary, upstreamChoice = true)

    // General behavior
    val FAST_TRANSITIONS = ScBoolPref("FAST_TRANSITIONS", true, R.string.sc_pref_fast_transitions_title, R.string.sc_pref_fast_transitions_summary, upstreamChoice = false)

    // Chat overview
    val COMPACT_APP_BAR = ScBoolPref("COMPACT_APP_BAR", true, R.string.sc_pref_compact_app_bar_title, R.string.sc_pref_compact_app_bar_summary, upstreamChoice = false)

    // Developer options
    val SC_DEV_QUICK_OPTIONS = ScBoolPref("SC_DEV_QUICK_OPTIONS", false, R.string.sc_pref_dev_quick_options, authorsChoice = true)
    val SC_RESTORE_DEFAULTS = ScActionablePref("SC_RESTORE_DEFAULTS", R.string.sc_pref_restore_defaults)
    val SC_RESTORE_UPSTREAM = ScActionablePref("SC_RESTORE_UPSTREAM", R.string.sc_pref_restore_element)
    val SC_RESTORE_AUTHORS_CHOICE = ScActionablePref("SC_RESTORE_AUTHORS_CHOICE", R.string.sc_pref_restore_authors_choice)

    // Tests to be removed before release
    val SC_TEST = ScStringListPref("TEST", "b", arrayOf("a", "b", "c"), arrayOf("A", "B", "C"), null, R.string.test)
    val SC_BUBBLE_BG_DARK_OUT = ScColorPref("SC_BUBBLE_BG_DARK_OUT", 0x008bc34a, R.string.test)
    val SC_BUBBLE_BG_LIGHT_OUT = ScColorPref("SC_BUBBLE_BG_LIGHT_OUT", 0x008bc34a, R.string.test)

    val scTweaks = ScPrefScreen(R.string.sc_pref_tweaks_title, null, listOf<AbstractScPref>(
        ScPrefCategory(R.string.sc_pref_category_general_appearance, null, listOf(
            SC_THEME,
            EL_TYPOGRAPHY,
        )),
        ScPrefCategory(R.string.sc_pref_category_general_behaviour, null, listOf(
            FAST_TRANSITIONS,
        )),
        ScPrefCategory(R.string.sc_pref_category_chat_overview, null, listOf(
            COMPACT_APP_BAR,
        )),
        ScPrefCategory(CommonStrings.common_developer_options, null, listOf(
            SC_DEV_QUICK_OPTIONS,
            SC_RESTORE_DEFAULTS,
            SC_RESTORE_UPSTREAM,
            SC_RESTORE_AUTHORS_CHOICE,
        )),
        ScPrefCategory(R.string.test, null, listOf(
            ScPrefScreen(R.string.test, null, listOf(
                SC_TEST,
                SC_BUBBLE_BG_DARK_OUT,
                SC_BUBBLE_BG_LIGHT_OUT,
            )),
        )),
    ))

    val devQuickTweaks = listOf(
        ScPrefCategory(R.string.sc_pref_category_general_appearance, null, listOf(
            SC_THEME,
            EL_TYPOGRAPHY,
        )),
        FAST_TRANSITIONS,
    )

    val devQuickTweaksInRoom = devQuickTweaks + listOf(
        // To be filled with some bubble design settings probably?
    )
}

class ScPreferencesStore(context: Context) {
    private val store = context.dataStore

    suspend fun <T>setSetting(scPref: ScPref<T>, value: T) {
        val key = scPref.key ?: return
        store.edit { prefs ->
            prefs[key] = value
        }
    }

    suspend fun <T>setSettingTypesafe(scPref: ScPref<T>, value: Any?) {
        val v = scPref.ensureType(value)
        if (v == null) {
            Timber.e("Cannot set typesafe setting for ${scPref.key}, $value")
            return
        }
        setSetting(scPref, v)
    }

    fun <T> settingFlow(scPref: ScPref<T>): Flow<T> {
        val key = scPref.key ?: return emptyFlow()
        return store.data.map { prefs ->
            prefs[key] ?: scPref.defaultValue
        }
    }

    @Composable
    fun <T>settingState(scPref: ScPref<T>, context: CoroutineContext = EmptyCoroutineContext): State<T> = settingFlow(scPref).collectAsState(scPref.defaultValue, context)

    suspend fun reset() {
        store.edit { it.clear() }
    }
}

fun List<AbstractScPref>.collectScPrefs(): List<ScPref<*>> = this.flatMap { pref ->
    when (pref) {
        is ScPrefContainer -> pref.prefs.collectScPrefs()
        is ScPref<*> -> listOf(pref)
        is ScActionablePref -> emptyList()
    }
}

@Composable
fun <R>List<ScPref<*>>.prefValMap(v: @Composable (ScPref<*>) -> R) = associate { it.sKey to v(it) }
@Composable
fun List<ScPref<out Any>>.prefMap() = prefValMap { p -> p }

@Composable
fun scPrefs(): ScPreferencesStore {
    val appContext = LocalContext.current.applicationContext
    return remember {
        ScPreferencesStore(appContext)
    }
}

fun ScPrefContainer.forEachPreference(block: (ScPref<*>) -> Unit) {
    prefs.forEach {
        if (it is ScPrefContainer) {
            it.forEachPreference(block)
        }
        if (it is ScPref<*>) {
            block(it)
        }
    }
}
