package io.element.android.libraries.designsystem.theme.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import chat.schildi.theme.ScTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarColors.toScTopAppBarColors(isEmpty: Boolean): TopAppBarColors {
    val bg = ScTheme.exposures.appBarBg
    return if (bg == null || isEmpty) {
        this
    } else {
        this.copy(containerColor = bg, scrolledContainerColor = bg)
    }
}
