package chat.schildi.components.preferences

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import chat.schildi.lib.preferences.ScPrefs
import chat.schildi.theme.ScTheme
import com.godaddy.android.colorpicker.ClassicColorPicker
import com.godaddy.android.colorpicker.HsvColor
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.atomic.atoms.RedIndicatorAtom
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.components.preferences.components.preferenceIcon
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.messageFromMeBackground
import io.element.android.libraries.designsystem.theme.messageFromOtherBackground
import io.element.android.libraries.designsystem.toEnabledColor
import io.element.android.libraries.designsystem.toSecondaryEnabledColor

@Composable
private fun resolveColorPrefInTheme(darkTheme: Boolean, resolve: @Composable () -> Color?): Color? {
    var resolved by remember { mutableStateOf<Color?>(null) }
    ScTheme(darkTheme = darkTheme, applySystemBarsUpdate = false) {
        resolved = resolve()
    }
    return resolved
}

@Composable
fun resolveColorRenderedThemeDefault(key: String): Color? {
    return when (key) {
        ScPrefs.BUBBLE_BG_LIGHT_INCOMING.sKey -> resolveColorPrefInTheme(darkTheme = false) {
            ScTheme.exposures.bubbleBgIncoming ?: ElementTheme.colors.messageFromOtherBackground
        }
        ScPrefs.BUBBLE_BG_LIGHT_OUTGOING.sKey -> resolveColorPrefInTheme(darkTheme = false) {
            ScTheme.exposures.bubbleBgOutgoing ?: ElementTheme.colors.messageFromMeBackground
        }
        ScPrefs.BUBBLE_BG_DARK_INCOMING.sKey -> resolveColorPrefInTheme(darkTheme = true) {
            ScTheme.exposures.bubbleBgIncoming ?: ElementTheme.colors.messageFromOtherBackground
        }
        ScPrefs.BUBBLE_BG_DARK_OUTGOING.sKey -> resolveColorPrefInTheme(darkTheme = true) {
            ScTheme.exposures.bubbleBgOutgoing ?: ElementTheme.colors.messageFromMeBackground
        }
        else -> null
    }
}

@Composable
fun PreferenceColorPreview(
    title: String,
    currentValue: Color?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    subtitle: String? = null,
    subtitleAnnotated: AnnotatedString? = null,
    loadingCurrentValue: Boolean = false,
    icon: ImageVector? = null,
    @DrawableRes iconResourceId: Int? = null,
    showIconAreaIfNoIcon: Boolean = false,
    showIconBadge: Boolean = false,
    showEndBadge: Boolean = false,
    tintColor: Color? = null,
    onClick: () -> Unit = {},
) {
    ListItem(
        modifier = modifier,
        enabled = enabled,
        onClick = onClick,
        leadingContent = preferenceIcon(
            icon = icon,
            iconResourceId = iconResourceId,
            showIconBadge = showIconBadge,
            showIconAreaIfNoIcon = showIconAreaIfNoIcon,
            tintColor = tintColor,
        ),
        headlineContent = {
            Text(
                style = ElementTheme.typography.fontBodyLgRegular,
                text = title,
                color = tintColor ?: enabled.toEnabledColor(),
            )
        },
        supportingContent = if (subtitle != null) {
            {
                Text(
                    style = ElementTheme.typography.fontBodyMdRegular,
                    text = subtitle,
                    color = tintColor ?: enabled.toSecondaryEnabledColor(),
                )
            }
        } else {
            subtitleAnnotated?.let {
                {
                    Text(
                        style = ElementTheme.typography.fontBodyMdRegular,
                        text = it,
                        color = tintColor ?: enabled.toSecondaryEnabledColor(),
                    )
                }
            }
        },
        trailingContent = if (currentValue != null || loadingCurrentValue || showEndBadge) {
            ListItemContent.Custom {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (currentValue != null) {
                        Spacer(Modifier.size(24.dp).background(currentValue))
                    } else if (loadingCurrentValue) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .progressSemantics()
                                .size(20.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    if (showEndBadge) {
                        val endBadgeStartPadding = if (currentValue != null || loadingCurrentValue) 16.dp else 0.dp
                        RedIndicatorAtom(
                            modifier = Modifier
                                .padding(start = endBadgeStartPadding)
                        )
                    }
                }
            }
        } else {
            null
        }
    )
}

@Composable
fun ColorPickerAlertDialog(initialValue: Color, defaultValue: Int, dismiss: () -> Unit, onChange: (Int) -> Unit) {
    var pickerColor by remember { mutableStateOf(initialValue) }
    var textValue by remember { mutableStateOf(String.format("#%08X", initialValue.toArgb())) }
    // Color picker doesn't refresh on external color change unfortunately...
    var refreshPickerKey by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = dismiss,
        confirmButton = {
            TextButton(onClick = {
                onChange(pickerColor.toArgb())
                dismiss()
            }) {
                androidx.compose.material3.Text(stringResource(io.element.android.libraries.ui.strings.R.string.action_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onChange(defaultValue)
                dismiss()
            }) {
                androidx.compose.material3.Text(stringResource(io.element.android.libraries.ui.strings.R.string.action_reset))
            }
        },
        text = {
            Column(Modifier.fillMaxWidth()) {
                key(refreshPickerKey) {
                    ClassicColorPicker(
                        modifier = Modifier.fillMaxWidth().height(320.dp).padding(10.dp),
                        color = HsvColor.from(pickerColor),
                        showAlphaBar = true,
                        onColorChanged = { hsvColor ->
                            pickerColor = hsvColor.toColor()
                            textValue = String.format("#%08X", pickerColor.toArgb())
                        }
                    )
                }
                Row(Modifier.fillMaxWidth()) {
                    TextField(
                        value = textValue,
                        onValueChange = {
                            // Remove everything that's not allowed hex
                            val sanitizedString = it.sanitizeHex()
                            // Force sanitized string to be displayed
                            textValue = "#${sanitizedString}"
                            try {
                                val intValue = Integer.parseUnsignedInt(sanitizedString, 16).let {
                                    if (sanitizedString.length <= 6) {
                                        // Add alpha channel
                                        it or 0xff000000.toInt()
                                    } else {
                                        it
                                    }
                                }
                                if (pickerColor.toArgb() != intValue) {
                                    pickerColor = Color(intValue)
                                    refreshPickerKey++
                                }
                            } catch (ignored: Throwable) {}
                        },
                        modifier = Modifier.weight(1f, fill = true),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                        ),
                    )
                }
            }
        }
    )
}

private fun String.sanitizeHex(): String {
    return uppercase().filter { it.isDigit() || (it in 'A'..'F') }.let {
        if (it.length > 8) it.substring(0, 8) else it
    }
}
