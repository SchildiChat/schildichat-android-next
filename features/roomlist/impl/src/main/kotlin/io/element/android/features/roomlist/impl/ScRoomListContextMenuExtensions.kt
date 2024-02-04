package io.element.android.features.roomlist.impl

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.ListItemStyle
import io.element.android.libraries.designsystem.theme.components.Text

@Composable
fun MarkAsReadActionItems(
    onMarkAsReadClicked: (Boolean) -> Unit,
    isUnread: Boolean,
) {
    if (isUnread) {
        ListItem(
            headlineContent = {
                Text(
                    text = stringResource(id = chat.schildi.lib.R.string.sc_action_mark_as_read),
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            modifier = Modifier.clickable { onMarkAsReadClicked(true) },
            leadingContent = ListItemContent.Icon(
                iconSource = IconSource.Vector(
                    Icons.Default.RemoveRedEye,
                    contentDescription = stringResource(id = chat.schildi.lib.R.string.sc_action_mark_as_read)
                )
            ),
            style = ListItemStyle.Primary,
        )
    } else {
        ListItem(
            headlineContent = {
                Text(
                    text = stringResource(id = chat.schildi.lib.R.string.sc_action_mark_as_unread),
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            modifier = Modifier.clickable { onMarkAsReadClicked(false) },
            leadingContent = ListItemContent.Icon(
                iconSource = IconSource.Vector(
                    Icons.Filled.NewReleases,
                    contentDescription = stringResource(id = chat.schildi.lib.R.string.sc_action_mark_as_unread)
                )
            ),
            style = ListItemStyle.Primary,
        )
    }
}
