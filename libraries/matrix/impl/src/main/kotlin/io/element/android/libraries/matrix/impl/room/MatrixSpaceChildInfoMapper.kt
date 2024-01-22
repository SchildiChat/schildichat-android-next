package io.element.android.libraries.matrix.impl.room

import io.element.android.libraries.matrix.api.room.MatrixSpaceChildInfo
import org.matrix.rustcomponents.sdk.SpaceChildInfo

object MatrixSpaceChildInfoMapper {
    fun map(spaceChildInfo: SpaceChildInfo): MatrixSpaceChildInfo = spaceChildInfo.let {
        return MatrixSpaceChildInfo(
            roomId = it.roomId,
            order = it.order,
            suggested = it.suggested,
        )
    }
}
