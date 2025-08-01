/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.analytics

import im.vector.app.features.analytics.plan.RoomModeration
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.powerlevels.RoomPowerLevelsValues
import io.element.android.services.analytics.api.AnalyticsService

internal fun RoomMember.Role.toAnalyticsMemberRole(): RoomModeration.Role = when (this) {
    is RoomMember.Role.Owner -> RoomModeration.Role.Administrator // TODO - distinguish creator from admin
    RoomMember.Role.Admin -> RoomModeration.Role.Administrator
    RoomMember.Role.Moderator -> RoomModeration.Role.Moderator
    RoomMember.Role.User -> RoomModeration.Role.User
}

internal fun analyticsMemberRoleForPowerLevel(powerLevel: Long): RoomModeration.Role {
    return RoomMember.Role.forPowerLevel(powerLevel).toAnalyticsMemberRole()
}

internal fun AnalyticsService.trackPermissionChangeAnalytics(initial: RoomPowerLevelsValues?, updated: RoomPowerLevelsValues) {
    if (updated.ban != initial?.ban) {
        capture(RoomModeration(RoomModeration.Action.ChangePermissionsBanMembers, analyticsMemberRoleForPowerLevel(updated.ban)))
    }
    if (updated.invite != initial?.invite) {
        capture(RoomModeration(RoomModeration.Action.ChangePermissionsInviteUsers, analyticsMemberRoleForPowerLevel(updated.invite)))
    }
    if (updated.kick != initial?.kick) {
        capture(RoomModeration(RoomModeration.Action.ChangePermissionsKickMembers, analyticsMemberRoleForPowerLevel(updated.kick)))
    }
    if (updated.sendEvents != initial?.sendEvents) {
        capture(RoomModeration(RoomModeration.Action.ChangePermissionsSendMessages, analyticsMemberRoleForPowerLevel(updated.sendEvents)))
    }
    if (updated.redactEvents != initial?.redactEvents) {
        capture(RoomModeration(RoomModeration.Action.ChangePermissionsRedactMessages, analyticsMemberRoleForPowerLevel(updated.redactEvents)))
    }
    if (updated.roomName != initial?.roomName) {
        capture(RoomModeration(RoomModeration.Action.ChangePermissionsRoomName, analyticsMemberRoleForPowerLevel(updated.roomName)))
    }
    if (updated.roomAvatar != initial?.roomAvatar) {
        capture(RoomModeration(RoomModeration.Action.ChangePermissionsRoomAvatar, analyticsMemberRoleForPowerLevel(updated.roomAvatar)))
    }
    if (updated.roomTopic != initial?.roomTopic) {
        capture(RoomModeration(RoomModeration.Action.ChangePermissionsRoomTopic, analyticsMemberRoleForPowerLevel(updated.roomTopic)))
    }
}
