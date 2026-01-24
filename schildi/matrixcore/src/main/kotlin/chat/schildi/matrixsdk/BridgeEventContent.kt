package chat.schildi.matrixsdk

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BridgeEventContent(
    @SerialName("bridgebot")
    val bridgeBot: String?,
    val protocol: Protocol,
) {
    @Serializable
    data class Protocol(
        @SerialName("avatar_url")
        val avatarUrl: String?,
        @SerialName("displayname")
        val displayName: String?,
        @SerialName("id")
        val id: String,
    )
}
