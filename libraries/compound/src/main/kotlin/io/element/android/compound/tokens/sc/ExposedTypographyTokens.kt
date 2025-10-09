package io.element.android.compound.tokens.sc

import androidx.compose.ui.text.TextStyle
import io.element.android.compound.tokens.generated.TypographyTokens

interface ExposedTypographyTokens {
    val fontBodyLgMedium: TextStyle
    val fontBodyLgRegular: TextStyle
    val fontBodyMdMedium: TextStyle
    val fontBodyMdRegular: TextStyle
    val fontBodySmMedium: TextStyle
    val fontBodySmRegular: TextStyle
    val fontBodyXsMedium: TextStyle
    val fontBodyXsRegular: TextStyle
    val fontHeadingLgBold: TextStyle
    val fontHeadingLgRegular: TextStyle
    val fontHeadingMdBold: TextStyle
    val fontHeadingMdRegular: TextStyle
    val fontHeadingSmMedium: TextStyle
    val fontHeadingSmRegular: TextStyle
    val fontHeadingXlBold: TextStyle
    val fontHeadingXlRegular: TextStyle
}

object ElTypographyTokens : ExposedTypographyTokens {
    override val fontBodyLgMedium = TypographyTokens.fontBodyLgMedium
    override val fontBodyLgRegular = TypographyTokens.fontBodyLgRegular
    override val fontBodyMdMedium = TypographyTokens.fontBodyMdMedium
    override val fontBodyMdRegular = TypographyTokens.fontBodyMdRegular
    override val fontBodySmMedium = TypographyTokens.fontBodySmMedium
    override val fontBodySmRegular = TypographyTokens.fontBodySmRegular
    override val fontBodyXsMedium = TypographyTokens.fontBodyXsMedium
    override val fontBodyXsRegular = TypographyTokens.fontBodyXsRegular
    override val fontHeadingLgBold = TypographyTokens.fontHeadingLgBold
    override val fontHeadingLgRegular = TypographyTokens.fontHeadingLgRegular
    override val fontHeadingMdBold = TypographyTokens.fontHeadingMdBold
    override val fontHeadingMdRegular = TypographyTokens.fontHeadingMdRegular
    override val fontHeadingSmMedium = TypographyTokens.fontHeadingSmMedium
    override val fontHeadingSmRegular = TypographyTokens.fontHeadingSmRegular
    override val fontHeadingXlBold = TypographyTokens.fontHeadingXlBold
    override val fontHeadingXlRegular = TypographyTokens.fontHeadingXlRegular
}
