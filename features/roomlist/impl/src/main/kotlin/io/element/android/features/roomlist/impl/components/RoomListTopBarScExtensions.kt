package io.element.android.features.roomlist.impl.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import chat.schildi.components.preferences.AutoRenderedDropdown
import chat.schildi.lib.preferences.ScPrefs
import chat.schildi.lib.preferences.value
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.theme.components.DropdownMenuItem
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun ScRoomListDropdownEntriesTop(
    onClick: () -> Unit,
    onMenuActionClicked: (RoomListMenuAction) -> Unit,
    onCreateRoomClicked: () -> Unit,
) {
    if (ScPrefs.SPACE_NAV.value()) {
        DropdownMenuItem(
            onClick = {
                onClick()
                onCreateRoomClicked()
            },
            text = { Text(stringResource(id = io.element.android.libraries.ui.strings.R.string.action_start_chat)) },
            leadingIcon = {
                Icon(
                    resourceId = CommonDrawables.ic_new_message,
                    tint = ElementTheme.materialColors.secondary,
                    contentDescription = null,
                )
            }
        )
    }
    DropdownMenuItem(
        onClick = {
            onClick()
            onMenuActionClicked(RoomListMenuAction.Settings)
        },
        text = { Text(stringResource(id = CommonStrings.common_settings)) },
        leadingIcon = {
            Icon(
                imageVector = CompoundIcons.SettingsSolid,
                tint = ElementTheme.materialColors.secondary,
                contentDescription = null,
            )
        }
    )
}


@Composable
fun ScRoomListDropdownEntriesBottom(
    onClick: () -> Unit,
    onMenuActionClicked: (RoomListMenuAction) -> Unit,
) {
    if (ScPrefs.SC_DEV_QUICK_OPTIONS.value()) {
        HorizontalDivider()
        ScPrefs.devQuickTweaksOverview.forEach {
            it.AutoRenderedDropdown(
                onClick = onClick,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Tune,
                        tint = ElementTheme.materialColors.secondary,
                        contentDescription = null,
                    )
                }
            )
        }
    }
}
