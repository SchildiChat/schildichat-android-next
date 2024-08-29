package io.element.android.features.messages.impl.timeline.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import chat.schildi.theme.ScTheme
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.matrix.api.timeline.item.event.LocalEventSendState
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
internal fun RowScope.ScTimestampIcon(
    sendState: LocalEventSendState?,
) {
    if (ScTheme.scTimeline && sendState is LocalEventSendState.Sending) {
        Spacer(modifier = Modifier.width(2.dp))
        Icon(
            modifier = Modifier.size(15.dp),
            imageVector = Icons.Outlined.AccessTime,
            contentDescription = stringResource(id = CommonStrings.common_sending),
            tint = MaterialTheme.colorScheme.secondary,
        )
    }

}

