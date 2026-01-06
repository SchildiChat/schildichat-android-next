/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securityandprivacy.impl.root

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.element.android.features.securityandprivacy.api.SecurityAndPrivacyPermissions
import io.element.android.features.securityandprivacy.impl.R
import io.element.android.features.securityandprivacy.impl.manageauthorizedspaces.AuthorizedSpacesSelection
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

data class SecurityAndPrivacyState(
    // the settings that are currently applied on the room.
    val savedSettings: SecurityAndPrivacySettings,
    // the settings the user wants to apply.
    val editedSettings: SecurityAndPrivacySettings,
    val homeserverName: String,
    val showEnableEncryptionConfirmation: Boolean,
    private val isKnockEnabled: Boolean,
    private val isSpaceSettingsEnabled: Boolean,
    val saveAction: AsyncAction<Unit>,
    val isSpace: Boolean,
    private val permissions: SecurityAndPrivacyPermissions,
    private val selectableJoinedSpaces: ImmutableSet<SpaceRoom>,
    private val spaceSelectionMode: SpaceSelectionMode,
    val eventSink: (SecurityAndPrivacyEvent) -> Unit
) {

    val isSpaceMemberSelectable = isSpaceSettingsEnabled && spaceSelectionMode != SpaceSelectionMode.None

    // Show SpaceMember option in two cases:
    // - the SpaceSettings FF is enabled
    // - SpaceMember is the current saved value
    val showSpaceMemberOption = savedSettings.roomAccess is SecurityAndPrivacyRoomAccess.SpaceMember || isSpaceMemberSelectable

    val showManageSpaceAction = spaceSelectionMode is SpaceSelectionMode.Multiple && editedSettings.roomAccess is SecurityAndPrivacyRoomAccess.SpaceMember

    val isAskToJoinSelectable = isKnockEnabled

    // Show Ask to join option in two cases:
    // - the Knock FF is enabled
    // - AskToJoin is the current saved value
    val showAskToJoinOption = savedSettings.roomAccess == SecurityAndPrivacyRoomAccess.AskToJoin || isAskToJoinSelectable

    val canBeSaved = savedSettings != editedSettings

    // Logic is in https://github.com/element-hq/element-meta/issues/3029
    val availableHistoryVisibilities = buildList {
        // Shared is always available
        add(SecurityAndPrivacyHistoryVisibility.Shared)
        if (editedSettings.roomAccess == SecurityAndPrivacyRoomAccess.Anyone && !editedSettings.isEncrypted) {
            add(SecurityAndPrivacyHistoryVisibility.WorldReadable)
        } else {
            add(SecurityAndPrivacyHistoryVisibility.Invited)
        }
    }
        .sorted()
        .toImmutableList()

    val showRoomAccessSection = permissions.canChangeRoomAccess

    val showRoomVisibilitySections = permissions.canChangeRoomVisibility &&
        editedSettings.roomAccess.canConfigureRoomVisibility()

    val showHistoryVisibilitySection = permissions.canChangeHistoryVisibility && !isSpace
    val showEncryptionSection = permissions.canChangeEncryption && !isSpace

    @Composable
    fun spaceMemberDescription(): String {
        return if (isSpaceMemberSelectable) {
            when (spaceSelectionMode) {
                is SpaceSelectionMode.Single -> {
                    val spaceName = spaceSelectionMode.spaceRoom?.displayName ?: spaceSelectionMode.spaceId.value
                    stringResource(R.string.screen_security_and_privacy_room_access_space_members_option_single_parent_description, spaceName)
                }
                is SpaceSelectionMode.None,
                is SpaceSelectionMode.Multiple -> stringResource(R.string.screen_security_and_privacy_room_access_space_members_option_multiple_parents_description)
            }
        } else {
            stringResource(R.string.screen_security_and_privacy_room_access_space_members_option_unavailable_description)
        }
    }

    fun getAuthorizedSpaceData(): AuthorizedSpacesSelection {
        return AuthorizedSpacesSelection(
            joinedSpaces = selectableJoinedSpaces.toImmutableList(),
            unknownSpaceIds = savedSettings.roomAccess.spaceIds().filter { spaceId ->
                selectableJoinedSpaces.none { it.roomId == spaceId }
            }.toImmutableList(),
            initialSelectedIds = editedSettings.roomAccess.spaceIds().toImmutableList()
        )
    }
}

data class SecurityAndPrivacySettings(
    val roomAccess: SecurityAndPrivacyRoomAccess,
    val isEncrypted: Boolean,
    val historyVisibility: SecurityAndPrivacyHistoryVisibility,
    val address: String?,
    val isVisibleInRoomDirectory: AsyncData<Boolean>
)

enum class SecurityAndPrivacyHistoryVisibility {
    // Order matters, and is from the most to the least restrictive
    Invited,
    Shared,
    WorldReadable;

    /**
     * Returns the fallback visibility when the current visibility is not available.
     */
    fun fallback(): SecurityAndPrivacyHistoryVisibility {
        return when (this) {
            Invited,
            Shared -> Shared
            WorldReadable -> Invited
        }
    }
}

sealed interface SpaceSelectionMode {
    data object None : SpaceSelectionMode
    data class Single(val spaceId: RoomId, val spaceRoom: SpaceRoom?) : SpaceSelectionMode
    data object Multiple : SpaceSelectionMode
}

sealed interface SecurityAndPrivacyRoomAccess {
    data object InviteOnly : SecurityAndPrivacyRoomAccess
    data object AskToJoin : SecurityAndPrivacyRoomAccess
    data object Anyone : SecurityAndPrivacyRoomAccess
    data class SpaceMember(val spaceIds: ImmutableList<RoomId>) : SecurityAndPrivacyRoomAccess

    fun canConfigureRoomVisibility(): Boolean {
        return when (this) {
            InviteOnly, is SpaceMember -> false
            AskToJoin, Anyone -> true
        }
    }

    fun spaceIds(): ImmutableList<RoomId> {
        return when (this) {
            is SpaceMember -> spaceIds
            else -> persistentListOf()
        }
    }
}

sealed class SecurityAndPrivacyFailures : Exception() {
    data object SaveFailed : SecurityAndPrivacyFailures()
}
