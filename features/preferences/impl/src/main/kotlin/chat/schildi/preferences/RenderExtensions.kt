package chat.schildi.preferences

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import chat.schildi.lib.preferences.ScPref
import io.element.android.libraries.designsystem.components.preferences.PreferenceSwitch
import timber.log.Timber

@Composable
fun ScPref<*>.AutoRendered(initial: Any, onChange: (Boolean) -> Unit) {
    when (this) {
        is ScPref.ScBoolPref -> return Rendered(initial, onChange)
        else -> {
            Timber.e("Not supported to render ScPref ${this.javaClass} for $sKey")
        }
    }
}

@Composable
fun ScPref.ScBoolPref.Rendered(initial: Any, onChange: (Boolean) -> Unit) {
    val v = ensureType(initial)
    if (v == null) {
        Timber.e("Invalid initial value $initial, should be boolean")
    }
    PreferenceSwitch(
        title = stringResource(id = titleRes),
        subtitle = summaryRes?.let { stringResource(id = it) },
        isChecked = v ?: defaultValue,
        onCheckedChange = { onChange(it) },
    )
}
