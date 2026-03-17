package io.element.android.libraries.matrix.api.room

import org.matrix.rustcomponents.sdk.BridgeStateProtocolInfo

data class BridgeState(
    val stateKey: String,
    val bridgeBotUserId: String?,
    val protocol: Protocol?
) {
    data class Protocol(
        val id: String?,
        val displayName: String?,
        val avatarUrl: String?,
    ) {
        companion object {
            fun from(protocol: BridgeStateProtocolInfo) = Protocol(
                id = protocol.id,
                displayName = protocol.displayName,
                avatarUrl = protocol.avatarUrl,
            )
        }
    }

    companion object {
        fun from(state: org.matrix.rustcomponents.sdk.BridgeState) = BridgeState(
            stateKey = state.stateKey,
            bridgeBotUserId = state.bridgeBotUserId,
            protocol = state.protocol?.let(Protocol::from)
        )
    }
}
