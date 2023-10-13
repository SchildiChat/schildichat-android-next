package chat.schildi.preferences

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import chat.schildi.lib.preferences.ScPref
import io.element.android.libraries.designsystem.components.dialogs.ListOption
import io.element.android.libraries.designsystem.components.dialogs.SingleSelectionDialog
import io.element.android.libraries.designsystem.components.preferences.PreferenceSwitch
import io.element.android.libraries.designsystem.components.preferences.PreferenceText
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
fun <T : Any>ScPref<T>.AutoRendered(initial: Any, onChange: (Any) -> Unit) {
    when (this) {
        is ScPref.ScBoolPref -> return Rendered(initial, onChange)
        is ScPref.ScListPref -> return Rendered(initial, onChange)
        else -> {
            Timber.e("Not supported to render ScPref ${this.javaClass} for $sKey")
        }
    }
}

@Composable
fun ScPref<Boolean>.Rendered(initial: Any, onChange: (Boolean) -> Unit) {
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
@Composable
fun <T : Any>ScPref.ScListPref<T>.Rendered(initial: Any, onChange: (Any) -> Unit) {
    val v = ensureType(initial)
    if (v == null) {
        Timber.e("Invalid initial value $initial")
    }
    val selectedIndex = itemKeys.indexOf(v).takeIf { it >= 0 } ?: itemKeys.indexOf(defaultValue)
    val selectedName = itemNames[selectedIndex]

    val coroutineScope = rememberCoroutineScope()
    val openDialog = remember { mutableStateOf(false) }

    PreferenceText(
        title = stringResource(id = titleRes),
        subtitle = summaryRes?.let { stringResource(id = it) } ?: selectedName,
        onClick = { openDialog.value = true }
    )
    if (openDialog.value) {
        SingleSelectionDialog(
            title = stringResource(id = titleRes),
            options = itemNames.mapIndexed { index, name ->
                ListOption(name, itemSummaries?.get(index))
            }.toImmutableList(),
            onOptionSelected = { index ->
                if (index != selectedIndex) {
                    onChange(itemKeys[index])
                }
                coroutineScope.launch {
                    openDialog.value = false
                }
            },
            onDismissRequest = { openDialog.value = false },
            initialSelection = selectedIndex,
        )
    }
}
