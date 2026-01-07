/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securityandprivacy.impl.manageauthorizedspaces

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
import io.element.android.features.securityandprivacy.impl.SecurityAndPrivacyNavigator
import io.element.android.libraries.architecture.appyx.launchMolecule
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.core.RoomId
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.first

@ContributesNode(RoomScope::class)
@AssistedInject
class ManageAuthorizedSpacesNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenter: ManageAuthorizedSpacesPresenter,
) : Node(buildContext, plugins = plugins) {
    private val navigator = plugins<SecurityAndPrivacyNavigator>().first()
    private val stateFlow = launchMolecule { presenter.present() }

    suspend fun waitForCompletion(data: AuthorizedSpacesSelection): ImmutableList<RoomId> {
        stateFlow.value.eventSink(ManageAuthorizedSpacesEvent.SetData(data))
        return stateFlow.first { it.isSelectionComplete }.selectedIds
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state by stateFlow.collectAsState()
        ManageAuthorizedSpacesView(
            state = state,
            onBackClick = { navigator.closeManageAuthorizedSpaces() },
            modifier = modifier
        )
    }
}
