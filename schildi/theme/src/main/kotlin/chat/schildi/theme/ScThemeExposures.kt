package chat.schildi.theme

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
  * This class collects all additional SC values that we want to add to Element's SemanticColors.
  */
@Stable
class ScThemeExposures(
    isScTheme: Boolean,
    horizontalDividerThickness: Dp,
    bubbleBgIncoming: Color?,
    bubbleBgOutgoing: Color?,
    appBarBg: Color?,
    bubbleRadius: Dp,
    timestampRadius: Dp,
    timestampOverlayBg: Color,
) {
    var isScTheme by mutableStateOf(isScTheme)
        private set
    var horizontalDividerThickness by mutableStateOf(horizontalDividerThickness)
        private set
    var bubbleBgIncoming by mutableStateOf(bubbleBgIncoming)
        private set
    var bubbleBgOutgoing by mutableStateOf(bubbleBgOutgoing)
        private set
    var appBarBg by mutableStateOf(appBarBg)
        private set
    var bubbleRadius by mutableStateOf(bubbleRadius)
        private set
    var timestampRadius by mutableStateOf(timestampRadius)
        private set
    var timestampOverlayBg by mutableStateOf(timestampOverlayBg)
        private set

    fun copy(
        isScTheme: Boolean = this.isScTheme,
        horizontalDividerThickness: Dp = this.horizontalDividerThickness,
        bubbleBgIncoming: Color? = this.bubbleBgIncoming,
        bubbleBgOutgoing: Color? = this.bubbleBgOutgoing,
        appBarBg: Color? = this.appBarBg,
        bubbleRadius: Dp = this.bubbleRadius,
        timestampRadius: Dp = this.timestampRadius,
        timestampOverlayBg: Color = this.timestampOverlayBg,
    ) = ScThemeExposures(
        isScTheme = isScTheme,
        horizontalDividerThickness = horizontalDividerThickness,
        bubbleBgIncoming = bubbleBgIncoming,
        bubbleBgOutgoing = bubbleBgOutgoing,
        appBarBg = appBarBg,
        bubbleRadius = bubbleRadius,
        timestampRadius = timestampRadius,
        timestampOverlayBg = timestampOverlayBg,
    )

    fun updateColorsFrom(other: ScThemeExposures) {
        isScTheme = other.isScTheme
        horizontalDividerThickness = other.horizontalDividerThickness
        bubbleBgIncoming = other.bubbleBgIncoming
        bubbleBgOutgoing = other.bubbleBgOutgoing
        appBarBg = other.appBarBg
        bubbleRadius = other.bubbleRadius
        timestampRadius = other.timestampRadius
        timestampOverlayBg = other.timestampOverlayBg
    }
}

internal val elementLightScExposures = ScThemeExposures(
    isScTheme = false,
    horizontalDividerThickness = 0.5.dp, /** [io.element.android.libraries.designsystem.theme.components.HorizontalDivider] */
    bubbleBgIncoming = null,
    bubbleBgOutgoing = null,
    appBarBg = null,
    bubbleRadius = 12.dp,
    timestampRadius = 10.0.dp,
    timestampOverlayBg = Color.Magenta, // unused for non-SC themes
)

internal val elementDarkScExposures = ScThemeExposures(
    isScTheme = false,
    horizontalDividerThickness = 0.5.dp, /** [io.element.android.libraries.designsystem.theme.components.HorizontalDivider] */
    bubbleBgIncoming = null,
    bubbleBgOutgoing = null,
    appBarBg = null,
    bubbleRadius = 12.dp,
    timestampRadius = 10.0.dp,
    timestampOverlayBg = Color.Magenta, // unused for non-SC themes
)
