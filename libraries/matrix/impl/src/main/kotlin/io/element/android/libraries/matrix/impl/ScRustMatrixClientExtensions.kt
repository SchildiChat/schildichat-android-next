package io.element.android.libraries.matrix.impl

import io.element.android.libraries.matrix.api.AccountDataRawEvent
import io.element.android.libraries.matrix.api.room.BridgeState
import io.element.android.libraries.matrix.api.room.SpaceCatchAllInfo

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

fun mapRustSpaceCatchAll(event: org.matrix.rustcomponents.sdk.SpaceCatchAllInfo) = SpaceCatchAllInfo(
    includeOrphans = event.includeOrphans,
    filterIsDirect = event.filterIsDm,
)

fun mapRustAccountDataRawEvent(event: org.matrix.rustcomponents.sdk.AccountDataRawEvent) = AccountDataRawEvent(
    eventType = event.eventType,
    content = event.content,
)
