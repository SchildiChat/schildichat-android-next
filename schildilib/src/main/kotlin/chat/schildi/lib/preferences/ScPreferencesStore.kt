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
import kotlinx.collections.immutable.immutableListOf
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "schildinext-preferences")

class ScPreferencesStore(context: Context) {
    private val store = context.dataStore

    val SC_THEME = ScBoolPref("SC_THEMES", true, R.string.sc_pref_sc_themes_title)
    val EL_TYPOGRAPHY = ScBoolPref("EL_TYPOGRAPHY", false, R.string.sc_pref_el_typography_title, R.string.sc_pref_el_typography_summary)
    val FAST_TRANSITIONS = ScBoolPref("FAST_TRANSITIONS", true, R.string.sc_pref_fast_transitions_title, R.string.sc_pref_fast_transitions_summary)
    val COMPACT_APP_BAR = ScBoolPref("COMPACT_APP_BAR", true, R.string.sc_pref_compact_app_bar_title, R.string.sc_pref_compact_app_bar_summary)
    val SC_TEST = ScStringListPref("TEST", "b", persistentListOf("a", "b", "c"), persistentListOf("A", "B", "C"), null, R.string.test)

    val scTweaks = listOf<AbstractScPref>(
        ScCategory(R.string.sc_pref_category_general_appearance, null, listOf(
            SC_THEME,
            EL_TYPOGRAPHY,
        )),
        ScCategory(R.string.sc_pref_category_general_behaviour, null, listOf(
            FAST_TRANSITIONS,
        )),
        ScCategory(R.string.sc_pref_category_chat_overview, null, listOf(
            COMPACT_APP_BAR,
        )),
        ScCategory(R.string.test, null, listOf(SC_TEST)),
    )

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
