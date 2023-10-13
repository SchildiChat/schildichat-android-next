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
import kotlinx.coroutines.flow.map
import timber.log.Timber
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "schildinext-preferences")

class ScPreferencesStore(context: Context) {
    private val store = context.dataStore

    val SC_THEME = ScPref.ScBoolPref("SC_THEMES", true, R.string.sc_pref_sc_themes_title)
    val EL_TYPOGRAPHY = ScPref.ScBoolPref("EL_TYPOGRAPHY", false, R.string.sc_pref_el_typography_title, R.string.sc_pref_el_typography_summary)
    val FAST_TRANSITIONS = ScPref.ScBoolPref("FAST_TRANSITIONS", true, R.string.sc_pref_fast_transitions_title, R.string.sc_pref_fast_transitions_summary)
    val COMPACT_APP_BAR = ScPref.ScBoolPref("COMPACT_APP_BAR", true, R.string.sc_pref_compact_app_bar_title, R.string.sc_pref_compact_app_bar_summary)
    //val SC_TEST = ScPref.ScStringListPref("TEST", "B", persistentListOf("A", "B", "C"), persistentListOf("a", "b", "c"), null, R.string.sc_pref_sc_themes_title)

    val scTweaks = listOf<ScPref<out Any>>(
        SC_THEME,
        EL_TYPOGRAPHY,
        COMPACT_APP_BAR,
        FAST_TRANSITIONS,
        //SC_TEST,
    )

    suspend fun <T>setSetting(scPref: ScPref<T>, value: T) {
        store.edit { prefs ->
            prefs[scPref.key] = value
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
        return store.data.map { prefs ->
            prefs[scPref.key] ?: scPref.defaultValue
        }
    }

    @Composable
    fun <T>settingState(scPref: ScPref<T>, context: CoroutineContext = EmptyCoroutineContext): State<T> = settingFlow(scPref).collectAsState(scPref.defaultValue, context)

    suspend fun reset() {
        store.edit { it.clear() }
    }
}

@Composable
fun <R>List<ScPref<out Any>>.prefValMap(v: @Composable (ScPref<out Any>) -> R) = associate { it.sKey to v(it) }
@Composable
fun List<ScPref<out Any>>.prefMap() = prefValMap { p -> p }

@Composable
fun scPrefs(): ScPreferencesStore {
    val appContext = LocalContext.current.applicationContext
    return remember {
        ScPreferencesStore(appContext)
    }
}
