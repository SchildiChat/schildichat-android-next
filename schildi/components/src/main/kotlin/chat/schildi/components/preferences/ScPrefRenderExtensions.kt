package chat.schildi.components.preferences

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import chat.schildi.lib.preferences.LocalScPreferencesStore
import chat.schildi.lib.preferences.ScActionablePref
import chat.schildi.lib.preferences.ScBoolPref
import chat.schildi.lib.preferences.ScColorPref
import chat.schildi.lib.preferences.ScDisclaimerPref
import chat.schildi.lib.preferences.ScIntPref
import chat.schildi.lib.preferences.ScPrefCategory
import chat.schildi.lib.preferences.ScListPref
import chat.schildi.lib.preferences.ScPref
import chat.schildi.lib.preferences.ScPrefCategoryCollapsed
import chat.schildi.lib.preferences.ScPrefScreen
import chat.schildi.lib.preferences.enabledState
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.components.dialogs.ListOption
import io.element.android.libraries.designsystem.components.dialogs.SingleSelectionDialog
import io.element.android.libraries.designsystem.components.preferences.PreferenceCategory
import io.element.android.libraries.designsystem.components.preferences.PreferenceSwitch
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.ListSupportingText
import io.element.android.libraries.designsystem.theme.components.SimpleAlertDialogContent
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
fun <T>ScPref<T>.AutoRendered(initial: Any, onChange: (Any) -> Unit) {
    when (this) {
        is ScBoolPref -> return Rendered(initial, onChange)
        is ScIntPref -> return Rendered(initial, onChange)
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
        content = content,
    )
}

@Composable
fun ScPrefCategoryCollapsed.Rendered(
    initial: Any,
    onChange: (Boolean) -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val v = ensureType(initial)
    if (v == null) {
        Timber.e("Invalid initial value $initial, should be boolean")
    }
    val expanded = v ?: defaultValue
    Column(Modifier.fillMaxWidth()) {
        // From ListSectionHeader, tweaked to have expand indicator
        HorizontalDivider(modifier = Modifier.padding(top = 16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onChange(!expanded)
                }
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(id = titleRes),
                style = ElementTheme.typography.fontBodyLgMedium,
                color = ElementTheme.colors.textPrimary,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            val chevronRotation = animateFloatAsState(if (expanded) -180f else 0f, label = "prefCategoryChevronRotation")
            Image(
                Icons.Default.KeyboardArrowDown,
                null,
                modifier = Modifier.size(16.dp).graphicsLayer {
                    rotationZ = chevronRotation.value
                }.align(Alignment.CenterVertically),
                colorFilter = ColorFilter.tint(ElementTheme.colors.textPrimary),
            )
        }
        AnimatedVisibility(
            visible = expanded,
            enter = slideInVertically { -it },
            exit = slideOutVertically { -it },
            modifier = Modifier.clip(RectangleShape),
        ) {
            Column(Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}

@Composable
fun ScPrefScreen.Rendered(
    onClick: () -> Unit
) {
    val enabled = LocalScPreferencesStore.current.enabledState(this).value
    ListItem(
        headlineContent = {
            Text(stringResource(id = titleRes))
        },
        supportingContent = summaryRes?.let {
            { Text(stringResource(it)) }
        },
        onClick = {
            if (enabled) {
                onClick()
            }
        },
        enabled = enabled,
    )
}

@Composable
fun ScActionablePref.Rendered(
    handleAction: (String) -> Unit
) {
    val enabled = LocalScPreferencesStore.current.enabledState(this).value
    ListItem(
        headlineContent = {
            Text(stringResource(id = titleRes))
        },
        supportingContent = summaryRes?.let {
            { Text(stringResource(it)) }
        },
        onClick = {
            if (enabled) {
                handleAction(key)
            }
        },
        enabled = enabled,
    )
}

@Composable
fun ScDisclaimerPref.Rendered() {
    Text(
        style = ElementTheme.typography.fontBodyMdRegular,
        text = stringResource(titleRes),
        color = ElementTheme.colors.textCriticalPrimary,
        modifier = Modifier.padding(16.dp),
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
        enabled = LocalScPreferencesStore.current.enabledState(this).value,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScIntPref.Rendered(initial: Any, onChange: (Any) -> Unit) {
    val v = ensureType(initial)
    if (v == null) {
        Timber.e("Invalid initial value $initial")
    }

    val openDialog = remember { mutableStateOf(false) }

    val enabled = LocalScPreferencesStore.current.enabledState(this).value

    val value = v ?: defaultValue

    ListItem(
        headlineContent = {
            Text(stringResource(id = titleRes))
        },
        supportingContent = summaryRes?.let {
            { Text(stringResource(it)) }
        },
        onClick = { if (enabled) openDialog.value = true },
        enabled = enabled,
    )
    if (openDialog.value) {
        val decoratedSubtitle: @Composable (() -> Unit)? = summaryRes?.let {
            @Composable {
                ListSupportingText(
                    text = stringResource(id = it, value, value),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
        BasicAlertDialog(
            modifier = Modifier,
            onDismissRequest = { openDialog.value = false },
        ) {
            val textValue = remember {
                val initialText = value.toString()
                mutableStateOf(TextFieldValue(text = initialText, selection = TextRange(0, initialText.length)))
            }
            val focusRequester = remember { FocusRequester() }
            fun onSubmitClick() {
                try {
                    val newValue = textValue.value.text.toInt().coerceIn(minValue, maxValue)
                    onChange(newValue)
                } catch (t: Throwable) {
                    Timber.w(t, "Failed to set int preference, invalid text input?")
                }
                openDialog.value = false
            }
            SimpleAlertDialogContent(
                title = stringResource(id = titleRes),
                subtitle = decoratedSubtitle,
                submitText = stringResource(CommonStrings.action_ok),
                cancelText = stringResource(CommonStrings.action_cancel),
                onSubmitClick = ::onSubmitClick,
                onCancelClick = { openDialog.value = false },
                applyPaddingToContents = false,
                enabled = textValue.value.text.toIntOrNull()?.let { it.coerceIn(minValue, maxValue) == it } == true,
            ) {
                androidx.compose.material3.TextField(
                    value = textValue.value,
                    onValueChange = { textValue.value = it },
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .fillMaxWidth(),
                    singleLine = true,
                    label = {
                        androidx.compose.material3.Text(stringResource(titleRes))
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    keyboardActions = KeyboardActions(onDone = { onSubmitClick() }),
                )
            }
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
        }
    }
}

@Composable
fun <T>ScListPref<T>.Rendered(initial: Any, onChange: (Any) -> Unit) {
    val v = ensureType(initial)
    if (v == null) {
        Timber.e("Invalid initial value $initial")
    }
    val names = stringArrayResource(itemNames)
    val summaries = itemSummaries?.let { stringArrayResource(it) }
    val selectedIndex = itemKeys.indexOf(v).takeIf { it >= 0 } ?: itemKeys.indexOf(defaultValue)
    val selectedName = names[selectedIndex]

    val coroutineScope = rememberCoroutineScope()
    val openDialog = remember { mutableStateOf(false) }

    val enabled = LocalScPreferencesStore.current.enabledState(this).value

    ListItem(
        headlineContent = {
            Text(stringResource(id = titleRes))
        },
        supportingContent = summaryRes?.let {
            { Text(stringResource(it)) }
        } ?: {
            Text(selectedName)
        },
        onClick = { if (enabled) openDialog.value = true },
        enabled = enabled,
    )
    if (openDialog.value) {
        SingleSelectionDialog(
            title = stringResource(id = titleRes),
            subtitle = summaryRes?.let { stringResource(id = it) },
            options = names.mapIndexed { index, name ->
                ListOption(name, summaries?.get(index))
            }.toImmutableList(),
            onSelectOption = { index ->
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

@Composable
fun ScColorPref.Rendered(initial: Any, onChange: (Any) -> Unit) {
    val v = ensureType(initial)
    if (v == null) {
        Timber.e("Invalid initial value $initial")
    }

    val openDialog = remember { mutableStateOf(false) }

    val enabled = LocalScPreferencesStore.current.enabledState(this).value
    val userValue = v?.let { ScColorPref.valueToColor(it) }
    val currentValue = userValue ?: resolveColorRenderedThemeDefault(sKey) ?: Color(v ?: defaultValue)
    val currentValueHex = currentValue.toArgb().let {
        if (it.toLong() and 0xff000000 == 0xff000000) {
            // No transparency
            String.format("#%06X", it and 0xffffff)
        } else {
            // With alpha
            String.format("#%08X", it)
        }
    }

    PreferenceColorPreview(
        title = stringResource(id = titleRes),
        currentValue = currentValue,
        subtitle = summaryRes?.let { stringResource(id = it, currentValueHex) } ?: if (userValue == null)
            stringResource(chat.schildi.lib.R.string.sc_color_pref_follow_theme_default)
        else
            stringResource(chat.schildi.lib.R.string.sc_color_pref_custom_color, currentValueHex),
        onClick = { if (enabled) openDialog.value = true },
        enabled = enabled,
    )
    if (openDialog.value) {
        ColorPickerAlertDialog(
            initialValue = currentValue,
            defaultValue = defaultValue,
            dismiss = { openDialog.value = false },
            onChange = onChange,
        )
    }
}
