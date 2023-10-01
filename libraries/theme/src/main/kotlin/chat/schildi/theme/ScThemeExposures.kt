package chat.schildi.theme

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.libraries.theme.compound.generated.internal.DarkDesignTokens
import io.element.android.libraries.theme.compound.generated.internal.LightDesignTokens

/**
  * This class collects all additional SC values that we want to add to Element's SemanticColors.
  */
@Stable
class ScThemeExposures(
    isScTheme: Boolean,
    horizontalDividerThickness: Dp,
    bubbleBgIncoming: Color,
    bubbleBgOutgoing: Color,
    appBarBg: Color,
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

    fun copy(
        isScTheme: Boolean = this.isScTheme,
        horizontalDividerThickness: Dp = this.horizontalDividerThickness,
        bubbleBgIncoming: Color = this.bubbleBgIncoming,
        bubbleBgOutgoing: Color = this.bubbleBgOutgoing,
        appBarBg: Color = this.appBarBg,
    ) = ScThemeExposures(
        isScTheme = isScTheme,
        horizontalDividerThickness = horizontalDividerThickness,
        bubbleBgIncoming = bubbleBgIncoming,
        bubbleBgOutgoing = bubbleBgOutgoing,
        appBarBg = appBarBg,
    )

    fun updateColorsFrom(other: ScThemeExposures) {
        isScTheme = other.isScTheme
        horizontalDividerThickness = other.horizontalDividerThickness
        bubbleBgIncoming = other.bubbleBgIncoming
        bubbleBgOutgoing = other.bubbleBgOutgoing
        appBarBg = other.appBarBg
    }
}

internal val elementLightScExposures = ScThemeExposures(
    isScTheme = false,
    horizontalDividerThickness = 0.5.dp, /** [io.element.android.libraries.designsystem.theme.components.HorizontalDivider] */
    bubbleBgIncoming = LightDesignTokens.colorGray300, /** [io.element.android.libraries.designsystem.theme.ColorAliases] SemanticColors.messageFromOtherBackground */
    bubbleBgOutgoing = LightDesignTokens.colorGray400, /** [io.element.android.libraries.designsystem.theme.ColorAliases] SemanticColors.messageFromMeBackground */
    appBarBg = Color(0xFFF9F9F9), /** [io.element.android.features.roomlist.impl.components.RoomListTopBar] hardcoded */
)

internal val elementDarkScExposures = ScThemeExposures(
    isScTheme = false,
    horizontalDividerThickness = 0.5.dp, /** [io.element.android.libraries.designsystem.theme.components.HorizontalDivider] */
    bubbleBgIncoming = DarkDesignTokens.colorGray400, /** [io.element.android.libraries.designsystem.theme.ColorAliases] SemanticColors.messageFromOtherBackground */
    bubbleBgOutgoing = DarkDesignTokens.colorGray500, /** [io.element.android.libraries.designsystem.theme.ColorAliases] SemanticColors.messageFromMeBackground */
    appBarBg = DarkDesignTokens.colorThemeBg, /** [io.element.android.features.roomlist.impl.components.RoomListTopBar] ElementTheme.materialColors.background */
)
