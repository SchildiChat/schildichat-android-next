package chat.schildi.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import io.element.android.libraries.theme.compound.*

private fun TextStyle.schildify(fontSizeOverride: TextUnit? = null): TextStyle {
    return copy(
        lineHeight = TextUnit.Unspecified,
        letterSpacing = TextUnit.Unspecified,
        fontSize = fontSizeOverride ?: fontSize
    )
}
internal val scTypography = Typography(
    headlineLarge = compoundHeadingXlRegular.schildify(),
    headlineMedium = compoundHeadingLgRegular.schildify(),
    headlineSmall = defaultHeadlineSmall.schildify(),
    titleLarge = compoundHeadingMdRegular.schildify(),
    titleMedium = compoundBodyLgMedium.schildify(),
    titleSmall = compoundBodyMdMedium.schildify(),
    bodyLarge = compoundBodyLgRegular.schildify(15.sp), // Used e.g. for message bubble content
    bodyMedium = compoundBodyMdRegular.schildify(),
    bodySmall = compoundBodySmRegular.schildify(),
    labelLarge = compoundBodyMdMedium_LabelLarge.schildify(),
    labelMedium = compoundBodySmMedium.schildify(),
    labelSmall = compoundBodyXsMedium.schildify()
)
