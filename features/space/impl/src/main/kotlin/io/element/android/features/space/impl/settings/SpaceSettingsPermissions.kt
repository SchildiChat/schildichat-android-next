/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl.settings

import io.element.android.features.roomdetailsedit.api.roomDetailsEditPermissions
import io.element.android.features.securityandprivacy.api.securityAndPrivacyPermissions
import io.element.android.libraries.matrix.api.room.StateEventType
import io.element.android.libraries.matrix.api.room.powerlevels.RoomPermissions

data class SpaceSettingsPermissions(
    val canEditDetails: Boolean,
    val canManageRolesAndPermissions: Boolean,
    val canManageSecurityAndPrivacy: Boolean,
){

    val hasAny = canEditDetails || canManageRolesAndPermissions || canManageSecurityAndPrivacy

    companion object {
        val DEFAULT = SpaceSettingsPermissions(
            canEditDetails = false,
            canManageRolesAndPermissions = false,
            canManageSecurityAndPrivacy = false,
        )
    }
}

fun  RoomPermissions.spaceSettingsPermissions(): SpaceSettingsPermissions {
    return SpaceSettingsPermissions(
        canEditDetails = roomDetailsEditPermissions().hasAny,
        canManageRolesAndPermissions = canOwnUserSendState(StateEventType.ROOM_POWER_LEVELS),
        canManageSecurityAndPrivacy = securityAndPrivacyPermissions().hasAny,
    )
}
