package io.element.android.libraries.designsystem.colors

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import chat.schildi.theme.ScPowerLevelColors

fun powerLevelToColor(powerLevel: Long?, colorScheme: ColorScheme): Color {
    return when {
        powerLevel == null -> ScPowerLevelColors.pl_null
        powerLevel >= 100 -> ScPowerLevelColors.pl_100
        powerLevel >= 95 -> ScPowerLevelColors.pl_95
        powerLevel >= 51 -> ScPowerLevelColors.pl_51
        powerLevel >= 50 -> ScPowerLevelColors.pl_50
        powerLevel >= 1 -> ScPowerLevelColors.pl_1
        powerLevel == 0L -> ScPowerLevelColors.pl_0
        else -> colorScheme.primary
    }
}

fun powerLevelToAvatarColors(powerLevel: Long?, colorScheme: ColorScheme): AvatarColors = AvatarColors(
    foreground = powerLevelToColor(powerLevel, colorScheme),
    // Unused so far, but easier for not modifying upstream code too much
    background = colorScheme.surfaceBright,
)
