package io.element.android.features.messages.impl

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import chat.schildi.components.preferences.AutoRenderedDropdown
import chat.schildi.lib.preferences.ScPrefs
import chat.schildi.lib.preferences.scPrefs
import io.element.android.libraries.designsystem.theme.components.DropdownMenu
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.utils.CommonDrawables

@Composable
internal fun RowScope.scMessagesViewTopBarActions() {
    if (scPrefs().settingState(scPref = ScPrefs.SC_DEV_QUICK_OPTIONS).value) {
        var showMenu by remember { mutableStateOf(false) }
        IconButton(
            onClick = { showMenu = !showMenu }
        ) {
            Icon(
                resourceId = CommonDrawables.ic_compound_overflow_vertical,
                contentDescription = null,
            )
        }
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            ScPrefs.devQuickTweaksInRoom.forEach {
                it.AutoRenderedDropdown(
                    onClick = { showMenu = false }
                )
            }
        }
        Spacer(Modifier.width(8.dp))
    }
}
