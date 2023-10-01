package chat.schildi.lib.preferences

import androidx.annotation.StringRes
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import timber.log.Timber

sealed interface ScPref<T> {
    val sKey: String
    val defaultValue: T
    val authorsChoice: T?

    @get:StringRes
    val titleRes: Int
    @get:StringRes
    val summaryRes: Int?

    val key: Preferences.Key<T>

    fun ensureType(value: Any?): T?

    interface ScListPref<T>: ScPref<T> {
        val itemKeys: Array<String>
        val itemNames: Array<T>
    }

    data class ScBoolPref(
        override val sKey: String,
        override val defaultValue: Boolean,
        @StringRes
        override val titleRes: Int,
        @StringRes
        override val summaryRes: Int? = null,
        override val authorsChoice: Boolean? = null,
    ): ScPref<Boolean> {
        override val key = booleanPreferencesKey(sKey)
        override fun ensureType(value: Any?): Boolean? {
            if (value !is Boolean) {
                Timber.e("Parse boolean failed of $sKey for ${value?.javaClass?.simpleName}")
                return null
            }
            return value
        }
    }

    data class ScStringListPref(
        override val sKey: String,
        override val defaultValue: String,
        override val itemKeys: Array<String>,
        override val itemNames: Array<String>,
        @StringRes
        override val titleRes: Int,
        @StringRes
        override val summaryRes: Int? = null,
        override val authorsChoice: String? = null,
    ): ScListPref<String> {
        override val key = stringPreferencesKey(sKey)
        override fun ensureType(value: Any?): String? {
            if (value !is String) {
                Timber.e("Parse string failed of $sKey for ${value?.javaClass?.simpleName}")
                return null
            }
            return value
        }
    }
}
