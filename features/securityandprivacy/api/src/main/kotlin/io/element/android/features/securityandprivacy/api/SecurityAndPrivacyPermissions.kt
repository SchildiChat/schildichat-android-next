/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securityandprivacy.api

import io.element.android.libraries.matrix.api.room.StateEventType
import io.element.android.libraries.matrix.api.room.powerlevels.RoomPermissions

data class SecurityAndPrivacyPermissions(
    val canChangeRoomAccess: Boolean,
    val canChangeHistoryVisibility: Boolean,
    val canChangeEncryption: Boolean,
    val canChangeRoomVisibility: Boolean,
) {
    val hasAny = canChangeRoomAccess ||
        canChangeHistoryVisibility ||
        canChangeEncryption ||
        canChangeRoomVisibility

    companion object {
        val DEFAULT = SecurityAndPrivacyPermissions(
            canChangeRoomAccess = false,
            canChangeHistoryVisibility = false,
            canChangeEncryption = false,
            canChangeRoomVisibility = false,
        )
    }
}

fun RoomPermissions.securityAndPrivacyPermissions(): SecurityAndPrivacyPermissions {
    return SecurityAndPrivacyPermissions(
        canChangeRoomAccess = canOwnUserSendState(StateEventType.ROOM_JOIN_RULES),
        canChangeHistoryVisibility = canOwnUserSendState(StateEventType.ROOM_HISTORY_VISIBILITY),
        canChangeEncryption = canOwnUserSendState(StateEventType.ROOM_ENCRYPTION),
        canChangeRoomVisibility = canOwnUserSendState(StateEventType.ROOM_CANONICAL_ALIAS),
    )
}
