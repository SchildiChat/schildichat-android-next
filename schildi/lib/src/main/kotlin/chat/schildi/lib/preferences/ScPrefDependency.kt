package chat.schildi.lib.preferences

import android.os.Parcelable
import androidx.datastore.preferences.core.Preferences
import kotlinx.parcelize.Parcelize

@Parcelize
sealed interface ScPrefDependency<T> : Parcelable {
    val pref: ScPref<T>

    fun fulfilledFor(value: T): Boolean
    fun fulfilledFor(preferences: Preferences): Boolean
}

@Parcelize
data class ScPrefEnabledDependency(
    override val pref: ScPref<Boolean>
) : ScPrefDependency<Boolean> {
    override fun fulfilledFor(value: Boolean): Boolean = value
    override fun fulfilledFor(preferences: Preferences): Boolean = fulfilledFor(preferences[pref.key!!] ?: pref.defaultValue)
}

fun ScPref<Boolean>.toDependency(): ScPrefDependency<*> = ScPrefEnabledDependency(this)
fun ScPref<Boolean>.asDependencies(): List<ScPrefDependency<*>> = listOf(toDependency())
