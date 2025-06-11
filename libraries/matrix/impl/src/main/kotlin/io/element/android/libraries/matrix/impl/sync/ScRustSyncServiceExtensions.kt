package io.element.android.libraries.matrix.impl.sync

import chat.schildi.lib.preferences.ScPreferencesStore
import chat.schildi.lib.preferences.ScPrefs
import io.element.android.libraries.matrix.api.sync.SyncState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map

@OptIn(FlowPreview::class)
fun Flow<SyncState>.maybeDebounceSyncState(scPreferencesStore: ScPreferencesStore): Flow<SyncState> =
    combine(scPreferencesStore.settingFlow(ScPrefs.DEBOUNCE_OFFLINE_STATE)) { a, b -> Pair(a, b) }
        .debounce { if (it.second && it.first == SyncState.Offline) 1000 else 0 }
        .map { it.first }
