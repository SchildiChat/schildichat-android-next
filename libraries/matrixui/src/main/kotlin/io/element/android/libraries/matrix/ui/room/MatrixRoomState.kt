/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.room

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.MessageEventType
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.isDm
import io.element.android.libraries.matrix.api.room.powerlevels.canBan
import io.element.android.libraries.matrix.api.room.powerlevels.canHandleKnockRequests
import io.element.android.libraries.matrix.api.room.powerlevels.canInvite
import io.element.android.libraries.matrix.api.room.powerlevels.canKick
import io.element.android.libraries.matrix.api.room.powerlevels.canRedactOther
import io.element.android.libraries.matrix.api.room.powerlevels.canRedactOwn
import io.element.android.libraries.matrix.api.room.powerlevels.canSendMessage

@Composable
fun MatrixRoom.canSendMessageAsState(type: MessageEventType, updateKey: Long): State<Boolean> {
    return produceState(initialValue = true, key1 = updateKey) {
        value = canSendMessage(type).getOrElse { true }
    }
}

@Composable
fun MatrixRoom.canInviteAsState(updateKey: Long): State<Boolean> {
    return produceState(initialValue = false, key1 = updateKey) {
        value = canInvite().getOrElse { false }
    }
}

@Composable
fun MatrixRoom.canRedactOwnAsState(updateKey: Long): State<Boolean> {
    return produceState(initialValue = false, key1 = updateKey) {
        value = canRedactOwn().getOrElse { false }
    }
}

@Composable
fun MatrixRoom.canRedactOtherAsState(updateKey: Long): State<Boolean> {
    return produceState(initialValue = false, key1 = updateKey) {
        value = canRedactOther().getOrElse { false }
    }
}

@Composable
fun MatrixRoom.canCall(updateKey: Long): State<Boolean> {
    return produceState(initialValue = false, key1 = updateKey) {
        value = canUserJoinCall(sessionId).getOrElse { false }
    }
}

@Composable
fun MatrixRoom.canPinUnpin(updateKey: Long): State<Boolean> {
    return produceState(initialValue = false, key1 = updateKey) {
        value = canUserPinUnpin(sessionId).getOrElse { false }
    }
}

@Composable
fun MatrixRoom.isDmAsState(): State<Boolean> {
    return produceState(initialValue = false) {
        roomInfoFlow.collect { value = it.isDm }
    }
}

@Composable
fun MatrixRoom.canKickAsState(updateKey: Long): State<Boolean> {
    return produceState(initialValue = false, key1 = updateKey) {
        value = canKick().getOrElse { false }
    }
}

@Composable
fun MatrixRoom.canBanAsState(updateKey: Long): State<Boolean> {
    return produceState(initialValue = false, key1 = updateKey) {
        value = canBan().getOrElse { false }
    }
}

@Composable
fun MatrixRoom.canHandleKnockRequestsAsState(updateKey: Long): State<Boolean> {
    return produceState(initialValue = false, key1 = updateKey) {
        value = canHandleKnockRequests().getOrElse { false }
    }
}

@Composable
fun MatrixRoom.userPowerLevelAsState(updateKey: Long): State<Long> {
    return produceState(initialValue = 0, key1 = updateKey) {
        value = userRole(sessionId)
            .getOrDefault(RoomMember.Role.USER)
            .powerLevel
    }
}

@Composable
fun MatrixRoom.isOwnUserAdmin(): Boolean {
    val roomInfo by roomInfoFlow.collectAsState()
    val powerLevel = roomInfo.userPowerLevels[sessionId] ?: 0L
    return RoomMember.Role.forPowerLevel(powerLevel) == RoomMember.Role.ADMIN
}

@Composable
fun MatrixRoom.rawName(): String? {
    val roomInfo by roomInfoFlow.collectAsState()
    return roomInfo.rawName
}

@Composable
fun MatrixRoom.topic(): String? {
    val roomInfo by roomInfoFlow.collectAsState()
    return roomInfo.topic
}

@Composable
fun MatrixRoom.avatarUrl(): String? {
    val roomInfo by roomInfoFlow.collectAsState()
    return roomInfo.avatarUrl
}
