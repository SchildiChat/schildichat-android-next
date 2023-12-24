package chat.schildi.theme.extensions

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import chat.schildi.theme.ScTheme

@Composable
inline fun Modifier.scOrElse(
    forSc: Modifier,
    crossinline forEl: @Composable Modifier.() -> Modifier,
) =
    if (ScTheme.exposures.isScTheme) {
        then(forSc)
    } else {
        this.forEl()
    }
