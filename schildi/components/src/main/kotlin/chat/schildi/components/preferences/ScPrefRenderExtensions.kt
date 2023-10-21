package chat.schildi.components.preferences

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import chat.schildi.lib.preferences.ScBoolPref
import chat.schildi.lib.preferences.ScColorPref
import chat.schildi.lib.preferences.ScPrefCategory
import chat.schildi.lib.preferences.ScListPref
import chat.schildi.lib.preferences.ScPref
import chat.schildi.lib.preferences.ScPrefScreen
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import io.element.android.libraries.designsystem.components.dialogs.ListOption
import io.element.android.libraries.designsystem.components.dialogs.SingleSelectionDialog
import io.element.android.libraries.designsystem.components.preferences.PreferenceCategory
import io.element.android.libraries.designsystem.components.preferences.PreferenceSwitch
import io.element.android.libraries.designsystem.components.preferences.PreferenceText
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
fun <T>ScPref<T>.AutoRendered(initial: Any, onChange: (Any) -> Unit) {
    when (this) {
        is ScBoolPref -> return Rendered(initial, onChange)
        is ScListPref -> return Rendered(initial, onChange)
        is ScColorPref -> return Rendered(initial, onChange)
        else -> {
            Timber.e("Not supported to render ScPref ${this.javaClass} for $sKey")
        }
    }
}

@Composable
fun ScPrefCategory.Rendered(
    content: @Composable ColumnScope.() -> Unit,
) {
    PreferenceCategory(
        title = stringResource(id = titleRes),
        content = content
    )
}

@Composable
fun ScPrefScreen.Rendered(
    onClick: () -> Unit
) {
    PreferenceText(
        title = stringResource(id = titleRes),
        onClick = onClick,
    )
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
fun <T>ScListPref<T>.Rendered(initial: Any, onChange: (Any) -> Unit) {
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
                    itemKeys[index]?.let {  k ->
                        onChange(k)
                    }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScColorPref.Rendered(initial: Any, onChange: (Any) -> Unit) {
    val v = ensureType(initial)
    if (v == null) {
        Timber.e("Invalid initial value $initial")
    }

    val openDialog = remember { mutableStateOf(false) }
    val controller = rememberColorPickerController()

    PreferenceText(
        title = stringResource(id = titleRes),
        subtitle = summaryRes?.let { stringResource(id = it) } ?: v?.let{ String.format("#%08X", it) },
        onClick = { openDialog.value = true }
    )
    if (openDialog.value) {
        AlertDialog(
            onDismissRequest = { openDialog.value = false },
        ) {
            // TODO some buttons, inspired by SimpleAlertDialogContent?
            // - ok to apply
            // - cancel to abort
            // - reset to restore default
            HsvColorPicker(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(450.dp)
                    .padding(10.dp),
                controller = controller,
                initialColor = Color(v ?: defaultValue),
                onColorChanged = { colorEnvelope: ColorEnvelope ->
                    onChange(colorEnvelope.color.toArgb()) // TODO only when clicking ok
                }
            )
        }
    }
}
