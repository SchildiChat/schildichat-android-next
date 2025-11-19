/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securityandprivacy.impl.root

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.securityandprivacy.api.SecurityAndPrivacyPermissions
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData

open class SecurityAndPrivacyStateProvider : PreviewParameterProvider<SecurityAndPrivacyState> {
    override val values: Sequence<SecurityAndPrivacyState>
        get() = securityAndPrivacyStates(isSpace = false) + securityAndPrivacyStates(isSpace = true)
}

private fun securityAndPrivacyStates(isSpace: Boolean): Sequence<SecurityAndPrivacyState> = sequenceOf(
    aSecurityAndPrivacyState(isSpace = isSpace),
    aSecurityAndPrivacyState(
        editedSettings = aSecurityAndPrivacySettings(
            roomAccess = SecurityAndPrivacyRoomAccess.AskToJoin,
        ),
        isSpace = isSpace,
    ),
    aSecurityAndPrivacyState(
        editedSettings = aSecurityAndPrivacySettings(
            roomAccess = SecurityAndPrivacyRoomAccess.Anyone,
            isEncrypted = false,
        ),
        isSpace = isSpace,
    ),
    aSecurityAndPrivacyState(
        savedSettings = aSecurityAndPrivacySettings(
            roomAccess = SecurityAndPrivacyRoomAccess.SpaceMember
        ),
        isSpace = isSpace,
        isKnockEnabled = false,
    ),
    aSecurityAndPrivacyState(
        editedSettings = aSecurityAndPrivacySettings(
            roomAccess = SecurityAndPrivacyRoomAccess.Anyone,
            address = "#therapy:myserver.xyz"
        ),
        isSpace = isSpace,
    ),
    aSecurityAndPrivacyState(
        editedSettings = aSecurityAndPrivacySettings(
            isVisibleInRoomDirectory = AsyncData.Loading()
        ),
        isSpace = isSpace,
    ),
    aSecurityAndPrivacyState(
        editedSettings = aSecurityAndPrivacySettings(
            isVisibleInRoomDirectory = AsyncData.Success(true)
        ),
        isSpace = isSpace,
    ),
    aSecurityAndPrivacyState(
        showEncryptionConfirmation = true,
        isSpace = isSpace,
    ),
    aSecurityAndPrivacyState(
        saveAction = AsyncAction.Loading,
        isSpace = isSpace,
    ),
    aSecurityAndPrivacyState(
        savedSettings = aSecurityAndPrivacySettings(
            roomAccess = SecurityAndPrivacyRoomAccess.AskToJoin
        ),
        isSpace = isSpace,
        isKnockEnabled = false,
    ),
)

fun aSecurityAndPrivacySettings(
    roomAccess: SecurityAndPrivacyRoomAccess = SecurityAndPrivacyRoomAccess.InviteOnly,
    isEncrypted: Boolean = true,
    address: String? = null,
    historyVisibility: SecurityAndPrivacyHistoryVisibility = SecurityAndPrivacyHistoryVisibility.SinceSelection,
    isVisibleInRoomDirectory: AsyncData<Boolean> = AsyncData.Uninitialized,
) = SecurityAndPrivacySettings(
    roomAccess = roomAccess,
    isEncrypted = isEncrypted,
    address = address,
    historyVisibility = historyVisibility,
    isVisibleInRoomDirectory = isVisibleInRoomDirectory
)

fun aSecurityAndPrivacyState(
    savedSettings: SecurityAndPrivacySettings = aSecurityAndPrivacySettings(),
    editedSettings: SecurityAndPrivacySettings = savedSettings,
    homeserverName: String = "myserver.xyz",
    showEncryptionConfirmation: Boolean = false,
    saveAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    permissions: SecurityAndPrivacyPermissions = SecurityAndPrivacyPermissions(
        canChangeRoomAccess = true,
        canChangeHistoryVisibility = true,
        canChangeEncryption = true,
        canChangeRoomVisibility = true
    ),
    isKnockEnabled: Boolean = true,
    isSpace: Boolean = false,
    eventSink: (SecurityAndPrivacyEvents) -> Unit = {}
) = SecurityAndPrivacyState(
    editedSettings = editedSettings,
    savedSettings = savedSettings,
    homeserverName = homeserverName,
    showEnableEncryptionConfirmation = showEncryptionConfirmation,
    saveAction = saveAction,
    isKnockEnabled = isKnockEnabled,
    permissions = permissions,
    isSpace = isSpace,
    eventSink = eventSink
)
