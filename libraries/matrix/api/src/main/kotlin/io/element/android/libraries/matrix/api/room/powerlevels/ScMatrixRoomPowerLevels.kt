package io.element.android.libraries.matrix.api.room.powerlevels

import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.StateEventType

suspend fun MatrixRoom.canManageSpaceChildren(): Result<Boolean> = canUserSendState(sessionId, StateEventType.SPACE_CHILD)
