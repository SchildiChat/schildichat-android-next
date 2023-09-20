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
    horizontalDividerThickness: Dp,
    bubbleBgIncoming: Color,
    bubbleBgOutgoing: Color,
) {
    var horizontalDividerThickness by mutableStateOf(horizontalDividerThickness)
        private set
    var bubbleBgIncoming by mutableStateOf(bubbleBgIncoming)
        private set
    var bubbleBgOutgoing by mutableStateOf(bubbleBgOutgoing)
        private set

    fun copy(
        horizontalDividerThickness: Dp = this.horizontalDividerThickness,
        bubbleBgIncoming: Color = this.bubbleBgIncoming,
        bubbleBgOutgoing: Color = this.bubbleBgOutgoing,
    ) = ScThemeExposures(
        horizontalDividerThickness = horizontalDividerThickness,
        bubbleBgIncoming = bubbleBgIncoming,
        bubbleBgOutgoing = bubbleBgOutgoing,
    )

    fun updateColorsFrom(other: ScThemeExposures) {
        horizontalDividerThickness = other.horizontalDividerThickness
        bubbleBgIncoming = other.bubbleBgIncoming
        bubbleBgOutgoing = other.bubbleBgOutgoing
    }
}

internal val elementLightScExposures = ScThemeExposures(
    horizontalDividerThickness = 0.5.dp, /** [io.element.android.libraries.designsystem.theme.components.HorizontalDivider] */
    bubbleBgIncoming = LightDesignTokens.colorGray300, /** [io.element.android.libraries.designsystem.theme.ColorAliases] SemanticColors.messageFromOtherBackground */
    bubbleBgOutgoing = LightDesignTokens.colorGray400, /** [io.element.android.libraries.designsystem.theme.ColorAliases] SemanticColors.messageFromMeBackground */
)

internal val elementDarkScExposures = ScThemeExposures(
    horizontalDividerThickness = 0.5.dp, /** [io.element.android.libraries.designsystem.theme.components.HorizontalDivider] */
    bubbleBgIncoming = DarkDesignTokens.colorGray400, /** [io.element.android.libraries.designsystem.theme.ColorAliases] SemanticColors.messageFromOtherBackground */
    bubbleBgOutgoing = DarkDesignTokens.colorGray500, /** [io.element.android.libraries.designsystem.theme.ColorAliases] SemanticColors.messageFromMeBackground */
)
