package io.element.android.features.messages.impl.topbars

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import chat.schildi.components.preferences.AutoRenderedDropdown
import chat.schildi.lib.R
import chat.schildi.lib.preferences.ScPrefs
import chat.schildi.lib.preferences.value
import chat.schildi.lib.util.formatUnreadCount
import chat.schildi.theme.ScTheme
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.messages.impl.SharedHistoryIcon
import io.element.android.features.messages.impl.pinned.banner.PinnedMessagesBannerState
import io.element.android.features.messages.impl.takeIfIsValidEventId
import io.element.android.features.messages.impl.timeline.TimelineEvent
import io.element.android.features.roomcall.api.RoomCallState
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.icons.CompoundDrawables
import io.element.android.libraries.designsystem.theme.components.DropdownMenu
import io.element.android.libraries.designsystem.theme.components.DropdownMenuItem
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.encryption.identity.IdentityState
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
internal fun IdentityState?.scOverride() = when {
    ScPrefs.VERBOSE_CONVERSATION_ICONS.value() -> this
    this == null -> null
    this == IdentityState.Verified -> null
    // Even when verbose icons are disabled, still hide error states
    else -> this
}

@Composable
internal fun SharedHistoryIcon.scOverride() = when {
    ScPrefs.VERBOSE_CONVERSATION_ICONS.value() -> this
    else -> SharedHistoryIcon.NONE
}

@Composable
internal fun ScTitleAdditions(state: io.element.android.features.messages.impl.MessagesState) {
    ScBridgeAvatar(state)
    ScNotEncryptedIndicator(state.isRoomEncrypted)
}

@Composable
private fun ScNotEncryptedIndicator(isRoomEncrypted: Boolean?) {
    if (isRoomEncrypted == false && ScPrefs.SC_TIMELINE_LAYOUT.value()) {
        Icon(
            modifier = Modifier.size(14.dp),
            imageVector = CompoundIcons.LockOff(),
            contentDescription = null,
            tint = ElementTheme.colors.iconInfoPrimary,
        )
    }
}

@Composable
private fun ScBridgeAvatar(state: io.element.android.features.messages.impl.MessagesState?) {
    state?.bridgeState?.firstOrNull { it.protocol?.avatarUrl != null }?.protocol?.let { protocol ->
        protocol.avatarUrl?.let { bridgeAvatar ->
            Avatar(
                avatarData = AvatarData(
                    id = protocol.id ?: "",
                    name = protocol.displayName,
                    url = bridgeAvatar,
                    size = AvatarSize.InviteSender,
                ),
                avatarType = AvatarType.Sc(CircleShape),
            )
        }
    }
}
