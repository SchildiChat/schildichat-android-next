package io.element.android.libraries.architecture.animation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import chat.schildi.lib.preferences.ScPrefs
import chat.schildi.lib.preferences.value
import com.bumble.appyx.core.navigation.transition.ModifierTransitionHandler
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.transitionhandler.rememberBackstackSlider

@Composable
fun <NavTarget> rememberDefaultTransitionHandler(): ModifierTransitionHandler<NavTarget, BackStack.State> {
    val fastTransitions = ScPrefs.FAST_TRANSITIONS.value()
    return key(fastTransitions) {
        rememberBackstackSlider(
            transitionSpec = {
                spring(
                    stiffness = if (fastTransitions) Spring.StiffnessHigh else Spring.StiffnessMediumLow
                )
            },
        )
    }
}
