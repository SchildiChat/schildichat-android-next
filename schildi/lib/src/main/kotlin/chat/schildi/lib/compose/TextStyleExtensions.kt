package chat.schildi.lib.compose

import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle

fun TextStyle.removeFontPadding(): TextStyle = copy(
    platformStyle = PlatformTextStyle(includeFontPadding = false)
)
