package chat.schildi.lib.preferences

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.di.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

private val Context.scPrefDataStore: DataStore<Preferences> by preferencesDataStore(name = "schildinext-preferences")

interface ScPreferencesStore {
    suspend fun <T>setSetting(scPref: ScPref<T>, value: T)
    suspend fun <T>setSettingTypesafe(scPref: ScPref<T>, value: Any?)
    fun <T> settingFlow(scPref: ScPref<T>): Flow<T>
    fun isEnabledFlow(scPref: AbstractScPref): Flow<Boolean>
    fun <T>getCachedOrDefaultValue(scPref: ScPref<T>): T
    suspend fun reset()
    suspend fun prefetch()
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class DefaultScPreferencesStore @Inject constructor(
    @ApplicationContext context: Context
) : ScPreferencesStore {
    private val store = context.scPrefDataStore

    // When listening to the settings flow, we want an appropriate initial value
    private val settingsCache = mutableMapOf<String, Any>()

    override suspend fun <T>setSetting(scPref: ScPref<T>, value: T) {
        val key = scPref.key ?: return
        store.edit { prefs ->
            prefs[key] = value
        }
        cacheSetting(scPref, value)
    }

    override suspend fun <T>setSettingTypesafe(scPref: ScPref<T>, value: Any?) {
        val v = scPref.ensureType(value)
        if (v == null) {
            Timber.e("Cannot set typesafe setting for ${scPref.key}, $value")
            return
        }
        setSetting(scPref, v)
    }

    override fun <T> settingFlow(scPref: ScPref<T>): Flow<T> {
        val key = scPref.key ?: return emptyFlow()
        return store.data.map { prefs ->
            if (isEnabled(prefs, scPref)) {
                prefs[key] ?: scPref.defaultValue
            } else {
                scPref.defaultValue
            }
        }
    }

    override fun <T>getCachedOrDefaultValue(scPref: ScPref<T>): T {
        return scPref.ensureType(settingsCache[scPref.sKey]) ?: scPref.defaultValue
    }

    override fun isEnabledFlow(scPref: AbstractScPref): Flow<Boolean> {
        return store.data.map { prefs ->
            isEnabled(prefs, scPref)
        }
    }

    private fun isEnabled(prefs: Preferences, scPref: AbstractScPref): Boolean {
        return scPref.dependencies.all {
            it.fulfilledFor(prefs)
        }
    }

    override suspend fun reset() {
        store.edit { it.clear() }
        settingsCache.clear()
    }

    override suspend fun prefetch() {
        ScPrefs.scTweaks.forEachPreferenceSuspend { pref ->
            val value = settingFlow(pref).firstOrNull()
            cacheSetting(pref, value)
        }
    }

    private fun <T>cacheSetting(scPref: ScPref<T>, value: Any?) {
        val v = scPref.ensureType(value)
        if (v == null) {
            settingsCache.remove(scPref.sKey)
        } else {
            settingsCache[scPref.sKey] = v
        }
    }
}

object FakeScPreferencesStore : ScPreferencesStore {
    override suspend fun <T> setSetting(scPref: ScPref<T>, value: T) {}
    override suspend fun <T> setSettingTypesafe(scPref: ScPref<T>, value: Any?) {}
    override fun <T> settingFlow(scPref: ScPref<T>): Flow<T> = emptyFlow()
    override fun isEnabledFlow(scPref: AbstractScPref): Flow<Boolean> = emptyFlow()
    override fun <T> getCachedOrDefaultValue(scPref: ScPref<T>): T = scPref.defaultValue

    override suspend fun reset() {}
    override suspend fun prefetch() {}
}

@Composable
fun <T>ScPreferencesStore.settingState(scPref: ScPref<T>, context: CoroutineContext = EmptyCoroutineContext): State<T> = settingFlow(scPref).collectAsState(getCachedOrDefaultValue(scPref), context)

@Composable
fun ScPreferencesStore.enabledState(scPref: AbstractScPref, context: CoroutineContext = EmptyCoroutineContext): State<Boolean> = isEnabledFlow(scPref).collectAsState(true, context)

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

val LocalScPreferencesStore = staticCompositionLocalOf<ScPreferencesStore> { FakeScPreferencesStore }

@Composable
fun <T>ScPref<T>.value(): T = LocalScPreferencesStore.current.settingState(this).value

@Composable
fun <T>ScPref<T>.state(): State<T> = LocalScPreferencesStore.current.settingState(this)

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

suspend fun ScPrefContainer.forEachPreferenceSuspend(block: suspend (ScPref<*>) -> Unit) {
    prefs.forEach {
        if (it is ScPrefContainer) {
            it.forEachPreferenceSuspend(block)
        }
        if (it is ScPref<*>) {
            block(it)
        }
    }
}
