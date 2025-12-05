/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.room.powerlevels

import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.MessageEventType
import io.element.android.libraries.matrix.api.room.StateEventType
import io.element.android.libraries.matrix.api.room.powerlevels.RoomPermissions

data class FakeRoomPermissions(
    val ownerCanBan: Boolean = false,
    val ownerCanInvite: Boolean = false,
    val ownerCanKick: Boolean = false,
    val ownerCanPinUnpin: Boolean = false,
    val ownerCanRedactOther: Boolean = false,
    val ownerCanRedactOwn: Boolean = false,
    val ownerCanTriggerRoomNotification: Boolean = false,
    val ownerCanSendMessage: (MessageEventType) -> Boolean = { false },
    val ownerCanSendState: (StateEventType) -> Boolean = { false },
    val userCanBan: (UserId) -> Boolean = { false },
    val userCanInvite: (UserId) -> Boolean = { false },
    val userCanKick: (UserId) -> Boolean = { false },
    val userCanPinUnpin: (UserId) -> Boolean = { false },
    val userCanRedactOther: (UserId) -> Boolean = { false },
    val userCanRedactOwn: (UserId) -> Boolean = { false },
    val userCanTriggerRoomNotification: (UserId) -> Boolean = { false },
    val userCanSendMessage: (UserId, MessageEventType) -> Boolean = { _, _ -> false },
    val userCanSendState: (UserId, StateEventType) -> Boolean = { _, _ -> false },
) : RoomPermissions {

    override fun canOwnUserBan(): Boolean = ownerCanBan
    override fun canOwnUserInvite(): Boolean = ownerCanInvite
    override fun canOwnUserKick(): Boolean = ownerCanKick
    override fun canOwnUserPinUnpin(): Boolean = ownerCanPinUnpin
    override fun canOwnUserRedactOther(): Boolean = ownerCanRedactOther
    override fun canOwnUserRedactOwn(): Boolean = ownerCanRedactOwn
    override fun canOwnUserSendMessage(message: MessageEventType): Boolean = ownerCanSendMessage(message)
    override fun canOwnUserSendState(stateEvent: StateEventType): Boolean = ownerCanSendState(stateEvent)
    override fun canOwnUserTriggerRoomNotification(): Boolean = ownerCanTriggerRoomNotification
    override fun canUserBan(userId: UserId): Boolean = userCanBan(userId)
    override fun canUserInvite(userId: UserId): Boolean = userCanInvite(userId)
    override fun canUserKick(userId: UserId): Boolean = userCanKick(userId)
    override fun canUserPinUnpin(userId: UserId): Boolean = userCanPinUnpin(userId)
    override fun canUserRedactOther(userId: UserId): Boolean = userCanRedactOther(userId)
    override fun canUserRedactOwn(userId: UserId): Boolean = userCanRedactOwn(userId)
    override fun canUserSendMessage(userId: UserId, message: MessageEventType): Boolean = userCanSendMessage(userId, message)
    override fun canUserSendState(userId: UserId, stateEvent: StateEventType): Boolean = userCanSendState(userId, stateEvent)
    override fun canUserTriggerRoomNotification(userId: UserId): Boolean = userCanTriggerRoomNotification(userId)

    override fun close() {
        // no-op for the fake
    }
}
