package chat.schildi.lib.preferences

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.core.os.BuildCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import chat.schildi.lib.BuildConfig
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.di.annotations.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import timber.log.Timber
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

private val Context.scPrefDataStore: DataStore<Preferences> by preferencesDataStore(name = "schildinext-preferences")

interface ScPreferencesStore {
    suspend fun <T>setSetting(scPref: ScPref<T>, value: T)
    suspend fun <T>setSettingTypesafe(scPref: ScPref<T>, value: Any?)
    fun <T> settingFlow(scPref: ScPref<T>): Flow<T>
    fun <T> combinedSettingValueAndEnabledFlow(transform: ((ScPref<*>) -> Any?, (ScPref<*>) -> Boolean) -> T): Flow<T>
    fun isEnabledFlow(scPref: AbstractScPref): Flow<Boolean>
    fun <T>getCachedOrDefaultValue(scPref: ScPref<T>): T
    suspend fun reset()
    suspend fun prefetch()

    fun <T> combinedSettingFlow(transform: ((ScPref<*>) -> Any?) -> T): Flow<T> = combinedSettingValueAndEnabledFlow { getPref, _ ->
        transform(getPref)
    }

    suspend fun <T>getSetting(scPref: ScPref<T>): T = settingFlow(scPref).first()
}

fun <T>ScPref<T>.safeLookup(getPref: (ScPref<*>) -> Any?): T {
    return ensureType(getPref(this)) ?: defaultValue
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
class DefaultScPreferencesStore(
    @ApplicationContext context: Context,
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
            val disabledValue = scPref.disabledValue
            if (isEnabled(prefs, scPref) || disabledValue == null) {
                prefs[key] ?: scPref.defaultValue
            } else {
                disabledValue
            }
        }
    }

    override fun <T> combinedSettingValueAndEnabledFlow(transform: ((ScPref<*>) -> Any?, (ScPref<*>) -> Boolean) -> T): Flow<T> {
        return store.data.map {  prefs ->
            transform(
                { scPref ->
                    val key = scPref.key ?: return@transform scPref.defaultValue
                    val disabledValue = scPref.disabledValue
                    if (isEnabled(prefs, scPref) || disabledValue == null) {
                        prefs[key] ?: scPref.defaultValue
                    } else {
                        disabledValue
                    }
                },
                { scPref ->
                    isEnabled(prefs, scPref)
                }
            )
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
    private fun shouldNotUsedInProduction() {
        val e = Exception("Should use CompositionLocalProvider with proper LocalScPreferencesStore")
        if (BuildConfig.DEBUG) {
            throw e
        } else {
            Timber.e(e, "No proper SC preferences store provided")
        }
    }
    override suspend fun <T> setSetting(scPref: ScPref<T>, value: T) = shouldNotUsedInProduction()
    override suspend fun <T> setSettingTypesafe(scPref: ScPref<T>, value: Any?) = shouldNotUsedInProduction()
    override fun <T> settingFlow(scPref: ScPref<T>): Flow<T> = emptyFlow<T>().also { shouldNotUsedInProduction() }
    override fun <T> combinedSettingValueAndEnabledFlow(transform: ((ScPref<*>) -> Any?, (ScPref<*>) -> Boolean) -> T): Flow<T> = emptyFlow<T>().also { shouldNotUsedInProduction() }
    override fun isEnabledFlow(scPref: AbstractScPref): Flow<Boolean> = emptyFlow<Boolean>().also { shouldNotUsedInProduction() }
    override fun <T> getCachedOrDefaultValue(scPref: ScPref<T>): T = scPref.defaultValue.also { shouldNotUsedInProduction() }

    override suspend fun reset() = shouldNotUsedInProduction()
    override suspend fun prefetch() = shouldNotUsedInProduction()
}

@Composable
fun <T>ScPreferencesStore.settingState(scPref: ScPref<T>, context: CoroutineContext = EmptyCoroutineContext): State<T> = settingFlow(scPref).collectAsState(getCachedOrDefaultValue(scPref), context)

@Composable
fun ScPreferencesStore.enabledState(scPref: AbstractScPref, context: CoroutineContext = EmptyCoroutineContext): State<Boolean> = isEnabledFlow(scPref).collectAsState(true, context)

fun List<AbstractScPref>.collectScPrefs(predicate: (ScPref<*>) -> Boolean = { true }): List<ScPref<*>> = this.flatMap { pref ->
    when (pref) {
        is ScPrefContainer -> pref.prefs.collectScPrefs(predicate).let {
            if (pref is ScPref<*>) {
                it + listOf(pref).filter(predicate)
            } else {
                it
            }
        }
        is ScPref<*> -> listOf(pref).filter(predicate)
        is ScDisclaimerPref,
        is ScActionablePref -> emptyList()
    }
}

@Composable
fun <R>List<ScPref<*>>.prefValMap(v: @Composable (ScPref<*>) -> R) = associate { it.sKey to v(it) }
@Composable
fun List<ScPref<out Any>>.prefMap() = prefValMap { p -> p }

// This did not work well enough to keep in sync with presenters...??? so init and de-init in ScApplication for now... TODO?
//val LocalScPreferencesStore = staticCompositionLocalOf<ScPreferencesStore> { FakeScPreferencesStore }
data class NotExactlyACompositionLocal<T>(val current: T)
var LocalScPreferencesStore = NotExactlyACompositionLocal<ScPreferencesStore>(FakeScPreferencesStore)

@Composable
fun <T>ScPref<T>.value(): T = LocalScPreferencesStore.current.settingState(this).value

@Composable
fun ScColorPref.userColor(): Color? = LocalScPreferencesStore.current.settingState(this).value.let { ScColorPref.valueToColor(it) }

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
