package io.element.android.features.messages.impl.timeline.components.event

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import chat.schildi.lib.preferences.ScPrefs
import chat.schildi.lib.preferences.value

@Composable
fun scLayoutDpUnspecified() = if (ScPrefs.SC_TIMELINE_LAYOUT.value()) Dp.Unspecified else null
