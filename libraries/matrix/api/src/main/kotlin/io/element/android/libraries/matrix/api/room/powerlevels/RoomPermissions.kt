/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room.powerlevels

import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.room.MessageEventType
import io.element.android.libraries.matrix.api.room.StateEventType

/**
 * Provides information about the permissions of users in a room.
 */
interface RoomPermissions : AutoCloseable {
    fun canOwnUserBan(): Boolean

    /**
     * Returns true if the current user is able to invite in the room.
     */
    fun canOwnUserInvite(): Boolean

    /**
     * Returns true if the current user is able to kick in the room.
     */
    fun canOwnUserKick(): Boolean

    /**
     * Returns true if the current user is able to pin or unpin events in the
     * room.
     */
    fun canOwnUserPinUnpin(): Boolean

    /**
     * Returns true if the current user user is able to redact messages of
     * other users in the room.
     */
    fun canOwnUserRedactOther(): Boolean

    /**
     * Returns true if the current user is able to redact their own messages in
     * the room.
     */
    fun canOwnUserRedactOwn(): Boolean

    /**
     * Returns true if the current user is able to send a specific message type
     * in the room.
     */
    fun canOwnUserSendMessage(message: MessageEventType): Boolean

    /**
     * Returns true if the current user is able to send a specific state event
     * type in the room.
     */
    fun canOwnUserSendState(stateEvent: StateEventType): Boolean

    /**
     * Returns true if the current user is able to trigger a notification in
     * the room.
     */
    fun canOwnUserTriggerRoomNotification(): Boolean

    /**
     * Returns true if the user with the given userId is able to ban in the
     * room.
     */
    fun canUserBan(userId: UserId): Boolean

    /**
     * Returns true if the user with the given userId is able to invite in the
     * room.
     */
    fun canUserInvite(userId: UserId): Boolean

    /**
     * Returns true if the user with the given userId is able to kick in the
     * room.
     */
    fun canUserKick(userId: UserId): Boolean

    /**
     * Returns true if the user with the given userId is able to pin or unpin
     * events in the room.
     */
    fun canUserPinUnpin(userId: UserId): Boolean

    /**
     * Returns true if the user with the given userId is able to redact
     * messages of other users in the room.
     */
    fun canUserRedactOther(userId: UserId): Boolean

    /**
     * Returns true if the user with the given userId is able to redact
     * their own messages in the room.
     */
    fun canUserRedactOwn(userId: UserId): Boolean

    /**
     * Returns true if the user with the given userId is able to send a
     * specific message type in the room.
     */
    fun canUserSendMessage(userId: UserId, message: MessageEventType): Boolean

    /**
     * Returns true if the user with the given userId is able to send a
     * specific state event type in the room.
     */
    fun canUserSendState(userId: UserId, stateEvent: StateEventType): Boolean

    /**
     * Returns true if the user with the given userId is able to trigger a
     * notification in the room.
     *
     * The call may fail if there is an error in getting the power levels.
     */
    fun canUserTriggerRoomNotification(userId: UserId): Boolean
}

fun RoomPermissions.canEditRoomDetails(): Boolean {
    return canOwnUserSendState(StateEventType.ROOM_NAME) ||
        canOwnUserSendState(StateEventType.ROOM_TOPIC) ||
        canOwnUserSendState(StateEventType.ROOM_AVATAR)
}

fun RoomPermissions.canManageKnockRequests(): Boolean {
    return canOwnUserInvite() || canOwnUserBan() || canOwnUserKick()
}

fun RoomPermissions.canEditSecurityAndPrivacy(): Boolean {
    return canOwnUserSendState(StateEventType.ROOM_JOIN_RULES) ||
        canOwnUserSendState(StateEventType.ROOM_HISTORY_VISIBILITY) ||
        canOwnUserSendState(StateEventType.ROOM_CANONICAL_ALIAS) ||
        canOwnUserSendState(StateEventType.ROOM_ENCRYPTION)
}

fun RoomPermissions.canEditRolesAndPermissions(): Boolean {
    return canOwnUserSendState(StateEventType.ROOM_POWER_LEVELS)
}
