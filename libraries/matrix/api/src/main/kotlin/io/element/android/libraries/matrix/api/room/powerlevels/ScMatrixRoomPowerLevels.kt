package io.element.android.libraries.matrix.api.room.powerlevels

import io.element.android.libraries.matrix.api.room.BaseRoom
import io.element.android.libraries.matrix.api.room.StateEventType

suspend fun BaseRoom.canManageSpaceChildren(): Result<Boolean> = this.roomPermissions().map { it.canUserSendState(sessionId, StateEventType.SpaceChild) }
