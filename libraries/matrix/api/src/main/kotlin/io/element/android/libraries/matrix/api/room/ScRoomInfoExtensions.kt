package io.element.android.libraries.matrix.api.room

data class BridgeState(
    val stateKey: String,
    val bridgeBotUserId: String?,
    val protocol: Protocol?
) {
    data class Protocol(
        val id: String?,
        val displayName: String?,
        val avatarUrl: String?,
    )
}
