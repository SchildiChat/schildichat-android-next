/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.datasource

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.mediaviewer.impl.model.MediaItem
import javax.inject.Inject

interface FocusedTimelineMediaGalleryDataSourceFactory {
    fun createFor(
        eventId: EventId,
        mediaItem: MediaItem.Event,
        onlyPinnedEvents: Boolean,
    ): MediaGalleryDataSource
}

@ContributesBinding(RoomScope::class)
class DefaultFocusedTimelineMediaGalleryDataSourceFactory @Inject constructor(
    private val room: JoinedRoom,
    private val timelineMediaItemsFactory: TimelineMediaItemsFactory,
    private val mediaItemsPostProcessor: MediaItemsPostProcessor,
) : FocusedTimelineMediaGalleryDataSourceFactory {
    override fun createFor(
        eventId: EventId,
        mediaItem: MediaItem.Event,
        onlyPinnedEvents: Boolean,
    ): MediaGalleryDataSource {
        return TimelineMediaGalleryDataSource(
            room = room,
            mediaTimeline = FocusedMediaTimeline(
                room = room,
                eventId = eventId,
                initialMediaItem = mediaItem,
                onlyPinnedEvents = onlyPinnedEvents,
            ),
            timelineMediaItemsFactory = timelineMediaItemsFactory,
            mediaItemsPostProcessor = mediaItemsPostProcessor,
        )
    }
}
