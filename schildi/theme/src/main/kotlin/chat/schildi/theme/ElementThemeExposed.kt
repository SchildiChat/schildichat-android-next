// https://stackoverflow.com/a/71894189
@file:Suppress("UNUSED", "INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package chat.schildi.theme

import io.element.android.compound.theme.toMaterialColorScheme
import io.element.android.compound.tokens.generated.compoundColorsDark
import io.element.android.compound.tokens.generated.compoundColorsLight
import io.element.android.compound.tokens.compoundTypography

/**
 * For seamless theme switching, we cannot do if (...) ElementTheme { } else ElementTheme { }, which would restart the app.
 * Unfortunately, the ElementTheme default values are internal, but we can "hack" us access to them by above Supress statement.
 */
val elMaterialColorSchemeDark = compoundColorsDark.toMaterialColorScheme()
val elMaterialColorSchemeLight = compoundColorsLight.toMaterialColorScheme()
val elColorsDark = compoundColorsDark
val elColorsLight = compoundColorsLight
val elTypography = compoundTypography
