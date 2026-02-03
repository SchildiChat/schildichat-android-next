package io.element.android.features.messages.impl.timeline.components.customreaction.picker

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import chat.schildi.lib.preferences.ScPrefs
import chat.schildi.lib.preferences.value
import io.element.android.libraries.designsystem.theme.components.SearchBar
import io.element.android.libraries.designsystem.theme.components.SearchBarResultState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ScEmojiPickerSearchBar(
    queryState: TextFieldState,
    active: Boolean,
    onActiveChange: (Boolean) -> Unit,
    placeHolderTitle: String,
    modifier: Modifier = Modifier,
    resultState: SearchBarResultState<T> = SearchBarResultState.Initial(),
    windowInsets: WindowInsets = SearchBarDefaults.windowInsets,
    resultHandler: @Composable ColumnScope.(T) -> Unit = {},
) {
    val focusRequester = remember { FocusRequester() }
    if (!ScPrefs.ALWAYS_SHOW_REACTION_SEARCH_BAR.value()) {
        if (active) {
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
        } else {
            return // Nothing to draw for non-sticky search bar
        }
    }

    SearchBar(
        modifier = modifier,
        queryState = queryState,
        resultState = resultState,
        active = active,
        onActiveChange = onActiveChange,
        windowInsets = windowInsets,
        placeHolderTitle = placeHolderTitle,
        focusRequester = focusRequester,
        resultHandler = resultHandler,
    )
}
