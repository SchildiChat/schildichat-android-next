package io.element.android.features.home.impl.datasource

import chat.schildi.lib.preferences.ScPreferencesStore
import chat.schildi.lib.preferences.ScPrefs
import io.element.android.features.home.impl.model.RoomListRoomSummary
import io.element.android.features.home.impl.model.RoomSummaryDisplayType
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

fun Flow<ImmutableList<RoomListRoomSummary>>.applyInviteFilterSetting(scPreferencesStore: ScPreferencesStore) =
    combine(scPreferencesStore.settingFlow(ScPrefs.HIDE_INVITES)) { origin, hideInvites ->
        if (hideInvites) {
            origin.filter { it.displayType != RoomSummaryDisplayType.INVITE }.toPersistentList()
        } else {
            origin
        }
    }
