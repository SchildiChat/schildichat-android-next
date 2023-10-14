package chat.schildi.lib.preferences

import android.os.Parcelable
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
}

@Parcelize
sealed interface ScPref<T> : AbstractScPref {
    val sKey: String
    val defaultValue: T
    val authorsChoice: T?

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
) : ScPrefContainer

@Parcelize
data class ScPrefCategory(
    override val titleRes: Int,
    override val summaryRes: Int?,
    override val prefs: List<AbstractScPref>,
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
): ScPref<Boolean> {
    @IgnoredOnParcel override val key = booleanPreferencesKey(sKey)
    override fun ensureType(value: Any?): Boolean? {
        if (value !is Boolean) {
            Timber.e("Parse boolean failed of $sKey for ${value?.javaClass?.simpleName}")
            return null
        }
        return value
    }
}

@Parcelize
sealed interface ScListPref<T>: ScPref<T> {
    val itemKeys: Array<T>
    val itemNames: Array<String>
    val itemSummaries: Array<String?>?
}

@Parcelize
data class ScStringListPref(
    override val sKey: String,
    override val defaultValue: String,
    override val itemKeys: Array<String>,
    override val itemNames: Array<String>,
    override val itemSummaries: Array<String?>?,
    @StringRes
    override val titleRes: Int,
    @StringRes
    override val summaryRes: Int? = null,
    override val authorsChoice: String? = null,
): ScListPref<String> {
    @IgnoredOnParcel override val key = stringPreferencesKey(sKey)
    override fun ensureType(value: Any?): String? {
        if (value !is String) {
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
