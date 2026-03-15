package chat.schildi.lib.preferences

import android.os.Parcelable
import androidx.datastore.preferences.core.Preferences
import kotlinx.parcelize.Parcelize

@Parcelize
sealed interface ScPrefDependency : Parcelable {
    fun fulfilledFor(getPref: (ScPref<*>) -> Any?): Boolean
}

@Parcelize
data class ScPrefEnabledDependency(
    val pref: ScPref<Boolean>,
    val expect: Boolean = true,
) : ScPrefDependency {
    private fun fulfilledFor(value: Boolean): Boolean = value == expect
    override fun fulfilledFor(getPref: (ScPref<*>) -> Any?): Boolean = fulfilledFor(pref.safeLookup(getPref))
}

@Parcelize
data class ScPrefFulfilledForAnyDependency(
    val dependencies: List<ScPrefDependency>,
) : ScPrefDependency {
    override fun fulfilledFor(getPref: (ScPref<*>) -> Any?): Boolean = dependencies.any { it.fulfilledFor(getPref) }
}

@Parcelize
data class ScPrefNotDependency(
    val dependency: ScPrefDependency,
) : ScPrefDependency {
    override fun fulfilledFor(getPref: (ScPref<*>) -> Any?): Boolean = !dependency.fulfilledFor(getPref)
}

fun ScPref<Boolean>.toDependency(expect: Boolean = true): ScPrefDependency = ScPrefEnabledDependency(this, expect)
fun ScPref<Boolean>.asDependencies(expect: Boolean = true): List<ScPrefDependency> = listOf(toDependency(expect))
fun ScPrefDependency.not() = ScPrefNotDependency(this)
