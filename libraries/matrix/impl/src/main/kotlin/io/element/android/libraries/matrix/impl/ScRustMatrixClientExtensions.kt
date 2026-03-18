package io.element.android.libraries.matrix.impl

import io.element.android.libraries.matrix.api.AccountDataRawEvent
import io.element.android.libraries.matrix.api.room.BridgeState

fun mapRustBridgeState(state: org.matrix.rustcomponents.sdk.BridgeState) = BridgeState(
    stateKey = state.stateKey,
    bridgeBotUserId = state.bridgeBotUserId,
    protocol = state.protocol?.map(),
)

fun org.matrix.rustcomponents.sdk.BridgeStateProtocolInfo.map() = BridgeState.Protocol(
    id = id,
    displayName = displayName,
    avatarUrl = avatarUrl,
)

fun mapRustAccountDataRawEvent(event: org.matrix.rustcomponents.sdk.AccountDataRawEvent) = AccountDataRawEvent(
    eventType = event.eventType,
    content = event.content,
)
