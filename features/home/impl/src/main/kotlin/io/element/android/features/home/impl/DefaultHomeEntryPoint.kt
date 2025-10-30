/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.home.api.HomeEntryPoint
import io.element.android.libraries.architecture.createNode

@ContributesBinding(AppScope::class)
class DefaultHomeEntryPoint : HomeEntryPoint {
    context(parentNode: Node)
    override fun createNode(buildContext: BuildContext, callback: HomeEntryPoint.Callback): Node {
        return parentNode.createNode<HomeFlowNode>(buildContext, listOf(callback))
    }
}
