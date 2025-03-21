package io.element.android.libraries.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import chat.schildi.lib.preferences.DefaultScPreferencesStore
import chat.schildi.lib.preferences.ScPreferencesStore

@Composable
fun rememberDefaultScPreferencesStore(): ScPreferencesStore {
    val context = LocalContext.current.applicationContext
    return remember(context) { DefaultScPreferencesStore(context) }
}
