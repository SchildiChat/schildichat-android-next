package io.element.android.appnav

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import chat.schildi.lib.preferences.ScPrefs
import chat.schildi.lib.preferences.value
import com.bumble.appyx.core.navigation.transition.ModifierTransitionHandler
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.transitionhandler.rememberBackstackFader
import com.bumble.appyx.navmodel.backstack.transitionhandler.rememberBackstackSlider

@Composable
fun scBackstackSlider(): ModifierTransitionHandler<LoggedInFlowNode.NavTarget, BackStack.State>? {
    return if (ScPrefs.FAST_TRANSITIONS.value()) {
        rememberBackstackSlider(
            transitionSpec = { spring(stiffness = Spring.StiffnessHigh) },
        )
    } else {
        null
    }
}

@Composable
fun scBackstackFader(): ModifierTransitionHandler<LoggedInFlowNode.NavTarget, BackStack.State>? {
    return if (ScPrefs.FAST_TRANSITIONS.value()) {
        rememberBackstackFader(
            transitionSpec = { spring(stiffness = Spring.StiffnessHigh) },
        )
    } else {
        null
    }
}
