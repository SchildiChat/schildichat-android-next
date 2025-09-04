/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.x.di

import dev.zacsweers.metro.ContributesBinding
import io.element.android.appnav.di.RoomComponentFactory
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.room.JoinedRoom
import dev.zacsweers.metro.Inject

@ContributesBinding(SessionScope::class)
@Inject
class DefaultRoomComponentFactory(
    private val sessionComponent: SessionComponent,
) : RoomComponentFactory {
    override fun create(room: JoinedRoom): Any {
        return sessionComponent.roomComponentFactory
            .create(room, room)
    }
}
