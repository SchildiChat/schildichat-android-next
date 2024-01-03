package chat.schildi.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import io.element.android.compound.tokens.generated.TypographyTokens
import io.element.android.compound.tokens.sc.ExposedTypographyTokens

private fun TextStyle.schildify(fontSizeOverride: TextUnit? = null): TextStyle {
    return copy(
        lineHeight = TextUnit.Unspecified,
        letterSpacing = TextUnit.Unspecified,
        fontSize = fontSizeOverride ?: fontSize
    )
}
internal val scTypography = Typography()

object ScTypographyTokens : ExposedTypographyTokens {
    override val fontBodyLgMedium = TypographyTokens.fontBodyLgMedium.schildify()
    override val fontBodyLgRegular = TypographyTokens.fontBodyLgRegular.schildify()
    override val fontBodyMdMedium = TypographyTokens.fontBodyMdMedium.schildify()
    override val fontBodyMdRegular = TypographyTokens.fontBodyMdRegular.schildify()
    override val fontBodySmMedium = TypographyTokens.fontBodySmMedium.schildify()
    override val fontBodySmRegular = TypographyTokens.fontBodySmRegular.schildify()
    override val fontBodyXsMedium = TypographyTokens.fontBodyXsMedium.schildify()
    override val fontBodyXsRegular = TypographyTokens.fontBodyXsRegular.schildify()
    override val fontHeadingLgBold = TypographyTokens.fontHeadingLgBold.schildify()
    override val fontHeadingLgRegular = TypographyTokens.fontHeadingLgRegular.schildify()
    override val fontHeadingMdBold = TypographyTokens.fontHeadingMdBold.schildify()
    override val fontHeadingMdRegular = TypographyTokens.fontHeadingMdRegular.schildify()
    override val fontHeadingSmMedium = TypographyTokens.fontHeadingSmMedium.schildify()
    override val fontHeadingSmRegular = TypographyTokens.fontHeadingSmRegular.schildify()
    override val fontHeadingXlBold = TypographyTokens.fontHeadingXlBold.schildify()
    override val fontHeadingXlRegular = TypographyTokens.fontHeadingXlRegular.schildify()
}
