/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.createroom.api

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import io.element.android.libraries.architecture.FeatureEntryPoint
import io.element.android.libraries.matrix.api.core.RoomId

interface CreateRoomEntryPoint : FeatureEntryPoint {
    fun nodeBuilder(parentNode: Node, buildContext: BuildContext): NodeBuilder
    interface NodeBuilder {
        fun callback(callback: Callback): NodeBuilder
        fun build(): Node
    }

    interface Callback : Plugin {
        fun onSuccess(roomId: RoomId)
    }
}
