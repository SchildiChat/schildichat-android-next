package io.element.android.features.messages.impl.timeline.components.event

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp

@Composable
fun scLayoutDpUnspecified() = Dp.Unspecified // This fix seems to be necessary on both SC and EleX layouts, but not on upstream EleX though?
