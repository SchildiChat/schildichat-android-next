package chat.schildi.lib.preferences

import android.content.Context
import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.element.android.libraries.di.annotations.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Context.scAppStateDataStore: DataStore<Preferences> by preferencesDataStore(name = "schildinext-appstate")
private val KEY_SPACE_SELECTION = stringPreferencesKey("SPACE_SELECTION")
private val KEY_LAST_PUSH_TS = longPreferencesKey("LAST_PUSH_TS")
private val KEY_LAST_PUSH_PROVIDER = stringPreferencesKey("LAST_PUSH_PROVIDER")
private val KEY_LAST_PUSH_GATEWAY = stringPreferencesKey("LAST_PUSH_GATEWAY")
private val KEY_LAST_PUSH_DISTRIBUTOR = stringPreferencesKey("LAST_PUSH_DISTRIBUTOR")
private val KEY_LAST_PUSH_DISTRIBUTOR_NAME = stringPreferencesKey("LAST_PUSH_DISTRIBUTOR_NAME")

interface ScAppStateStore {
    val store: DataStore<Preferences>
    suspend fun persistSpaceSelection(selection: List<String>)
    suspend fun loadInitialSpaceSelection(): List<String>
    suspend fun onPushReceived(provider: String?)
    suspend fun setPushGateway(gateway: String?)
    suspend fun setPushDistributor(distributor: String?, name: String?)
    suspend fun formatLastPushTs(): String
    suspend fun lastPushProvider(): String
    suspend fun lastPushGateway(): String
    suspend fun lastPushDistributor(): String?
    suspend fun lastPushDistributorName(): String?
}

@ContributesBinding(AppScope::class)
@Inject
class DefaultScAppStateStore(
    @ApplicationContext context: Context,
) : ScAppStateStore {
    override val store = context.scAppStateDataStore

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss ZZZ", Locale.US)

    override suspend fun persistSpaceSelection(selection: List<String>) {
        store.edit { prefs ->
            prefs[KEY_SPACE_SELECTION] = Json.encodeToString(selection)
        }
    }

    override suspend fun loadInitialSpaceSelection(): List<String> {
        return try {
            store.data.firstOrNull()?.get(KEY_SPACE_SELECTION)?.let { Json.decodeFromString<List<String>>(it) }.orEmpty()
        } catch (e: Throwable) {
            Timber.e(e, "Failed to restore initial space selection")
            emptyList()
        }
    }

    override suspend fun onPushReceived(provider: String?) {
        store.edit { prefs ->
            prefs[KEY_LAST_PUSH_TS] = System.currentTimeMillis()
            if (provider == null) {
                prefs.remove(KEY_LAST_PUSH_PROVIDER)
            } else {
                prefs[KEY_LAST_PUSH_PROVIDER] = provider
            }
        }
    }

    override suspend fun setPushGateway(gateway: String?) {
        store.edit { prefs ->
            if (gateway == null) {
                prefs.remove(KEY_LAST_PUSH_GATEWAY)
            } else {
                prefs[KEY_LAST_PUSH_GATEWAY] = gateway
            }
        }
    }

    override suspend fun setPushDistributor(distributor: String?, name: String?) {
        store.edit { prefs ->
            if (distributor == null) {
                prefs.remove(KEY_LAST_PUSH_DISTRIBUTOR)
            } else {
                prefs[KEY_LAST_PUSH_DISTRIBUTOR] = distributor
            }
            if (name == null) {
                prefs.remove(KEY_LAST_PUSH_DISTRIBUTOR_NAME)
            } else {
                prefs[KEY_LAST_PUSH_DISTRIBUTOR_NAME] = name
            }
        }
    }

    override suspend fun formatLastPushTs(): String {
        val lastPush = store.data.firstOrNull()?.get(KEY_LAST_PUSH_TS) ?: return "None"
        return dateFormat.format(Date(lastPush))
    }

    override suspend fun lastPushProvider(): String {
        return store.data.firstOrNull()?.get(KEY_LAST_PUSH_PROVIDER) ?: "None"
    }

    override suspend fun lastPushGateway(): String {
        return store.data.firstOrNull()?.get(KEY_LAST_PUSH_GATEWAY) ?: "None"
    }

    override suspend fun lastPushDistributor(): String? {
        return store.data.firstOrNull()?.get(KEY_LAST_PUSH_DISTRIBUTOR)
    }

    override suspend fun lastPushDistributorName(): String? {
        return store.data.firstOrNull()?.get(KEY_LAST_PUSH_DISTRIBUTOR_NAME)
    }
}

@Composable
fun ScAppStateStore.formatPushInfoOverview(): String {
    val data = store.data.collectAsState(null).value ?: return "No push info"
    val result = StringBuilder("Last push provider: ${data[KEY_LAST_PUSH_PROVIDER]}")
    val dateFormat = DateFormat.getLongDateFormat(LocalContext.current)
    val timeFormat = DateFormat.getTimeFormat(LocalContext.current)
    val formattedPushTs = data[KEY_LAST_PUSH_TS]?.let {
        val d = Date(it)
        "${dateFormat.format(d)} ${timeFormat.format(d)}"
    } ?: "none"
    result.append("\nLast push distributor: ${data[KEY_LAST_PUSH_DISTRIBUTOR_NAME]}")
    result.append("\nLast push distributor package: ${data[KEY_LAST_PUSH_DISTRIBUTOR]}")
    result.append("\nLast push gateway: ${data[KEY_LAST_PUSH_GATEWAY]}")
    result.append("\nLast push: $formattedPushTs")
    return result.toString()
}
