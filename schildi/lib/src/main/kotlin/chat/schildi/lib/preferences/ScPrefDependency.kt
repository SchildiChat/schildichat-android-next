package chat.schildi.lib.preferences

import android.os.Parcelable
import androidx.datastore.preferences.core.Preferences
import kotlinx.parcelize.Parcelize

@Parcelize
sealed interface ScPrefDependency : Parcelable {
    fun fulfilledFor(preferences: Preferences): Boolean
}

@Parcelize
data class ScPrefEnabledDependency(
    val pref: ScPref<Boolean>
) : ScPrefDependency {
    private fun fulfilledFor(value: Boolean): Boolean = value
    override fun fulfilledFor(preferences: Preferences): Boolean = fulfilledFor(preferences[pref.key!!] ?: pref.defaultValue)
}

@Parcelize
data class ScPrefFulfilledForAnyDependency(
    val dependencies: List<ScPrefDependency>,
) : ScPrefDependency {
    override fun fulfilledFor(preferences: Preferences): Boolean = dependencies.any { it.fulfilledFor(preferences) }
}

fun ScPref<Boolean>.toDependency(): ScPrefDependency = ScPrefEnabledDependency(this)
fun ScPref<Boolean>.asDependencies(): List<ScPrefDependency> = listOf(toDependency())
