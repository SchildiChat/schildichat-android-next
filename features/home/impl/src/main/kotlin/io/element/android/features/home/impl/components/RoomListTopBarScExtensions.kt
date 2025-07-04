package io.element.android.features.home.impl.components

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
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun ScRoomListDropdownEntriesTop(
    onClick: () -> Unit,
    onMenuActionClick: (RoomListMenuAction) -> Unit,
    onCreateRoomClick: () -> Unit,
) {
    if (!ScPrefs.SNC_FAB.value()) {
        DropdownMenuItem(
            onClick = {
                onClick()
                onCreateRoomClick()
            },
            text = { Text(stringResource(id = io.element.android.libraries.ui.strings.R.string.action_start_chat)) },
            leadingIcon = {
                Icon(
                    imageVector = CompoundIcons.Compose(),
                    tint = ElementTheme.materialColors.secondary,
                    contentDescription = null,
                )
            }
        )
    }
    DropdownMenuItem(
        onClick = {
            onClick()
            onMenuActionClick(RoomListMenuAction.Settings)
        },
        text = { Text(stringResource(id = CommonStrings.common_settings)) },
        leadingIcon = {
            Icon(
                imageVector = CompoundIcons.SettingsSolid(),
                tint = ElementTheme.materialColors.secondary,
                contentDescription = null,
            )
        }
    )
}


@Composable
fun ScRoomListDropdownEntriesBottom(
    onClick: () -> Unit,
    onMenuActionClick: (RoomListMenuAction) -> Unit,
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
