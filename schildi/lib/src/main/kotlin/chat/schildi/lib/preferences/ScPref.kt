package chat.schildi.lib.preferences

import android.os.Parcelable
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import timber.log.Timber

@Parcelize
sealed interface AbstractScPref : Parcelable {
    @get:StringRes
    val titleRes: Int

    @get:StringRes
    val summaryRes: Int?

    val dependencies: List<ScPrefDependency>
}

@Parcelize
sealed interface ScPref<T> : AbstractScPref {
    val sKey: String
    val defaultValue: T
    val authorsChoice: T?
    val upstreamChoice: T?

    val key: Preferences.Key<T>?

    fun ensureType(value: Any?): T?
}

@Parcelize
sealed interface ScPrefContainer : AbstractScPref {
    val prefs: List<AbstractScPref>
}

@Parcelize
data class ScPrefScreen(
    override val titleRes: Int,
    override val summaryRes: Int?,
    override val prefs: List<AbstractScPref>,
    override val dependencies: List<ScPrefDependency> = emptyList(),
) : ScPrefContainer

@Parcelize
data class ScPrefCategory(
    override val titleRes: Int,
    override val summaryRes: Int?,
    override val prefs: List<AbstractScPref>,
    override val dependencies: List<ScPrefDependency> = emptyList(),
) : ScPrefContainer

@Parcelize
data class ScBoolPref(
    override val sKey: String,
    override val defaultValue: Boolean,
    @StringRes
    override val titleRes: Int,
    @StringRes
    override val summaryRes: Int? = null,
    override val authorsChoice: Boolean? = null,
    override val upstreamChoice: Boolean? = null,
    override val dependencies: List<ScPrefDependency> = emptyList(),
): ScPref<Boolean> {
    @IgnoredOnParcel override val key = booleanPreferencesKey(sKey)
    override fun ensureType(value: Any?): Boolean? {
        if (value !is Boolean?) {
            Timber.e("Parse boolean failed of $sKey for ${value?.javaClass?.simpleName}")
            return null
        }
        return value
    }
}

@Parcelize
sealed interface ScListPref<T>: ScPref<T> {
    val itemKeys: Array<T>
    @get: ArrayRes
    val itemNames: Int
    @get: ArrayRes
    val itemSummaries: Int?
}

@Parcelize
data class ScStringListPref(
    override val sKey: String,
    override val defaultValue: String,
    override val itemKeys: Array<String>,
    @ArrayRes
    override val itemNames: Int,
    @ArrayRes
    override val itemSummaries: Int?,
    @StringRes
    override val titleRes: Int,
    @StringRes
    override val summaryRes: Int? = null,
    override val authorsChoice: String? = null,
    override val upstreamChoice: String? = null,
    override val dependencies: List<ScPrefDependency> = emptyList(),
): ScListPref<String> {
    @IgnoredOnParcel override val key = stringPreferencesKey(sKey)
    override fun ensureType(value: Any?): String? {
        if (value !is String?) {
            Timber.e("Parse string failed of $sKey for ${value?.javaClass?.simpleName}")
            return null
        }
        return value
    }
}

@Parcelize
data class ScColorPref(
    override val sKey: String,
    override val defaultValue: Int,
    @StringRes
    override val titleRes: Int,
    @StringRes
    override val summaryRes: Int? = null,
    override val authorsChoice: Int? = null,
    override val upstreamChoice: Int? = null,
    override val dependencies: List<ScPrefDependency> = emptyList(),
): ScPref<Int> {
    @IgnoredOnParcel override val key = intPreferencesKey(sKey)
    override fun ensureType(value: Any?): Int? {
        if (value !is Int) {
            Timber.e("Parse Int failed of $sKey for ${value?.javaClass?.simpleName}")
            return null
        }
        return value
    }
}

@Parcelize
data class ScActionablePref(
    val key: String,
    override val titleRes: Int,
    override val summaryRes: Int? = null,
    override val dependencies: List<ScPrefDependency> = emptyList(),
) : AbstractScPref

/*
@Parcelize
data class ScUpstreamFeatureFlagAliasPref(
    val featureFlag: FeatureFlags,
    override val titleRes: Int,
    override val summaryRes: Int? = null,
    override val dependencies: List<ScPrefDependency> = emptyList(),
) : ScPref<Boolean> {
    @IgnoredOnParcel override val sKey: String = featureFlag.key
    @IgnoredOnParcel override val defaultValue: Boolean = false // TODO featureFlag.defaultValue()
    @IgnoredOnParcel override val authorsChoice: Boolean? = null
    @IgnoredOnParcel override val upstreamChoice: Boolean? = null
    @IgnoredOnParcel override val key: Preferences.Key<Boolean>? = null

    override fun ensureType(value: Any?): Boolean? {
        if (value !is Boolean?) {
            Timber.e("Parse boolean failed of $sKey for ${value?.javaClass?.simpleName}")
            return null
        }
        return value
    }
}
 */
