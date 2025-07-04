package io.element.android.features.home.impl.filters

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import chat.schildi.lib.preferences.ScPrefs
import chat.schildi.lib.preferences.value
import io.element.android.features.home.impl.filters.selection.FilterSelectionStrategy

@Composable
internal fun ScClearRoomFiltersEffect(filterSelectionStrategy: FilterSelectionStrategy) {
    val isFeatureEnabled = ScPrefs.ELEMENT_ROOM_LIST_FILTERS.value()
    LaunchedEffect(isFeatureEnabled) {
        if (!isFeatureEnabled) {
            filterSelectionStrategy.clear()
        }
    }
}
