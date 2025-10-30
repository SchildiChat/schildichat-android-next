/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.features.space.impl.di.SpaceFlowScope
import io.element.android.libraries.architecture.appyx.launchMolecule

@ContributesNode(SpaceFlowScope::class)
@AssistedInject
class SpaceSettingsNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: SpaceSettingsPresenter,
) : Node(buildContext, plugins = plugins) {
    interface Callback : Plugin {
        fun onBackClick()

        fun onSpaceInfoClick()
        fun onMembersClick()
        fun onRolesAndPermissionsClick()
        fun onSecurityAndPrivacyClick()
        fun onLeaveSpaceClick()
    }

    private val callback = plugins<Callback>().single()
    private val stateFlow = launchMolecule { presenter.present() }

    @Composable
    override fun View(modifier: Modifier) {
        val state by stateFlow.collectAsState()
        SpaceSettingsView(
            state = state,
            modifier = modifier,
            onSpaceInfoClick = callback::onSpaceInfoClick,
            onBackClick = callback::onBackClick,
            onMembersClick = callback::onMembersClick,
            onRolesAndPermissionsClick = callback::onRolesAndPermissionsClick,
            onSecurityAndPrivacyClick = callback::onSecurityAndPrivacyClick,
            onLeaveSpaceClick = callback::onLeaveSpaceClick,
        )
    }
}
