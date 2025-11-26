/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securityandprivacy.impl.root

sealed interface SecurityAndPrivacyEvents {
    data object EditRoomAddress : SecurityAndPrivacyEvents
    data object Save : SecurityAndPrivacyEvents
    data object Exit : SecurityAndPrivacyEvents
    data object DismissExitConfirmation : SecurityAndPrivacyEvents
    data class ChangeRoomAccess(val roomAccess: SecurityAndPrivacyRoomAccess) : SecurityAndPrivacyEvents
    data object ToggleEncryptionState : SecurityAndPrivacyEvents
    data object CancelEnableEncryption : SecurityAndPrivacyEvents
    data object ConfirmEnableEncryption : SecurityAndPrivacyEvents
    data class ChangeHistoryVisibility(val historyVisibility: SecurityAndPrivacyHistoryVisibility) : SecurityAndPrivacyEvents
    data object ToggleRoomVisibility : SecurityAndPrivacyEvents
    data object DismissSaveError : SecurityAndPrivacyEvents
}
