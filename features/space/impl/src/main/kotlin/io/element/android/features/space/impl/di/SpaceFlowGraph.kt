/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl.di

import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides
import io.element.android.libraries.architecture.NodeFactoriesBindings
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.spaces.SpaceRoomList

@GraphExtension(SpaceFlowScope::class)
interface SpaceFlowGraph : NodeFactoriesBindings {
    @ContributesTo(SessionScope::class)
    @GraphExtension.Factory
    interface Factory {
        fun create(@Provides spaceRoomList: SpaceRoomList): SpaceFlowGraph
    }
}
