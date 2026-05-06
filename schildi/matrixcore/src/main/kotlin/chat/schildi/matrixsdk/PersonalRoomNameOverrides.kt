package chat.schildi.matrixsdk

import kotlinx.serialization.Serializable

const val ROOM_ACCOUNT_DATA_PERSONAL_ROOM_NAME = "de.gematik.msc4431.room.name.private"

@Serializable
data class RoomNamePrivateContent(
    val name: String?,
)
