package io.element.android.libraries.designsystem.atomic.atoms

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import chat.schildi.theme.ScBrandingColors

@Composable
fun ScLogoAtom(
    size: ElementLogoAtomSize,
    modifier: Modifier = Modifier,
    darkTheme: Boolean = isSystemInDarkTheme(),
) {
    val commonModifier = modifier.size(size.outerSize).clip(RoundedCornerShape(size.cornerRadius))
    val themedModifier = if (darkTheme) {
        commonModifier.border(size.borderWidth, ScBrandingColors.onboardingLogoOutlineDark, RoundedCornerShape(size.cornerRadius))
    } else {
        commonModifier.background(Brush.linearGradient(ScBrandingColors.onboardingIconBgColors))
    }
    Box(
        modifier = themedModifier,
        contentAlignment = Alignment.Center,
    ) {
        Image(
            modifier = Modifier
                .size(size.logoSize),
            painter = painterResource(id = chat.schildi.lib.R.drawable.sc_logo_atom),
            contentDescription = null
        )
    }
}
