package io.element.android.libraries.designsystem.colors

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import chat.schildi.theme.PowerLevelColors
import chat.schildi.theme.ScPowerLevelColors
import chat.schildi.theme.ScTheme
import io.element.android.compound.theme.AvatarColors
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.theme.avatarColors
import timber.log.Timber

@Composable
fun getPowerLevelColors(): PowerLevelColors {
    return when {
        ScTheme.yes -> ScPowerLevelColors
        else -> avatarColors().toPowerLevelColors()
    }
}

private fun List<AvatarColors>.toPowerLevelColors(): PowerLevelColors {
    if (size != 8) {
        Timber.e("Upstream avatar color length changed, is $size - need update to power level'ed colors, resorting back to SC theme!")
        return ScPowerLevelColors
    }
    return PowerLevelColors(
        pl_100 = this[1].foreground,
        pl_95 = this[4].foreground,
        pl_51 = this[5].foreground,
        pl_50 = this[0].foreground,
        pl_1 = this[7].foreground,
        pl_0 = this[2].foreground,
    )
}

fun PowerLevelColors.powerLevelToColor(powerLevel: Long?, colorScheme: ColorScheme): Color = powerLevel?.let { resolve(it) } ?: colorScheme.onSurface

fun PowerLevelColors.powerLevelToAvatarColors(powerLevel: Long?, colorScheme: ColorScheme): AvatarColors = AvatarColors(
    foreground = powerLevelToColor(powerLevel, colorScheme),
    // Unused so far, but easier for not modifying upstream code too much
    background = colorScheme.surfaceBright,
)

@Composable
fun powerLevelToAvatarColors(powerLevel: Long?) = getPowerLevelColors().powerLevelToAvatarColors(powerLevel, ElementTheme.materialColors)
