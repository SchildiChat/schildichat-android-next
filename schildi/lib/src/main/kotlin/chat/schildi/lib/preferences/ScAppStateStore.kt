package chat.schildi.lib.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber

private val Context.scAppStateDataStore: DataStore<Preferences> by preferencesDataStore(name = "schildinext-appstate")
private val KEY_SPACE_SELECTION = stringPreferencesKey("SPACE_SELECTION")

class ScAppStateStore(context: Context) {
    private val store = context.scAppStateDataStore

    suspend fun persistSpaceSelection(selection: List<String>) {
        store.edit { prefs ->
            prefs[KEY_SPACE_SELECTION] = Json.encodeToString(selection)
        }
    }

    suspend fun loadInitialSpaceSelection(): List<String> {
        return try {
            store.data.firstOrNull()?.get(KEY_SPACE_SELECTION)?.let { Json.decodeFromString<List<String>>(it) }.orEmpty()
        } catch (e: Throwable) {
            Timber.e(e, "Failed to restore initial space selection")
            emptyList()
        }
    }
}
