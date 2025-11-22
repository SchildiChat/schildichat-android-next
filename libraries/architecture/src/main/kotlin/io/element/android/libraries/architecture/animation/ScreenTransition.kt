/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.architecture.animation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import chat.schildi.lib.preferences.ScPrefs
import chat.schildi.lib.preferences.value
import com.bumble.appyx.core.navigation.transition.ModifierTransitionHandler
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.transitionhandler.rememberBackstackSlider

@Composable
fun <NavTarget> rememberDefaultTransitionHandler(): ModifierTransitionHandler<NavTarget, BackStack.State> {
    val fastTransitions = ScPrefs.FAST_TRANSITIONS.value()
    // "remember()" will not re-compose on settings change, so remember both values
    val upstreamSlider: ModifierTransitionHandler<NavTarget, BackStack.State> = rememberBackstackSlider(
        transitionSpec = { spring(stiffness = Spring.StiffnessMediumLow) },
    )
    val fastSlider: ModifierTransitionHandler<NavTarget, BackStack.State> = rememberBackstackSlider(
        transitionSpec = { spring(stiffness = Spring.StiffnessHigh) },
    )
    return if (fastTransitions) fastSlider else upstreamSlider
}
