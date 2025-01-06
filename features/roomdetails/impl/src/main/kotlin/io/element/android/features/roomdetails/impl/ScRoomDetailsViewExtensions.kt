package io.element.android.features.roomdetails.impl

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import chat.schildi.lib.preferences.ScPrefs
import chat.schildi.lib.preferences.value
import io.element.android.libraries.designsystem.components.preferences.PreferenceSwitch

@Composable
internal fun LowPriorityItem(
    isLowPriority: Boolean,
    onLowPriorityChanges: (Boolean) -> Unit,
) {
    if (ScPrefs.BURY_LOW_PRIORITY.value()) {
        PreferenceSwitch(
            icon = Icons.Default.Archive,
            title = stringResource(id = chat.schildi.lib.R.string.sc_action_low_priority),
            isChecked = isLowPriority,
            onCheckedChange = onLowPriorityChanges
        )
    }
}
