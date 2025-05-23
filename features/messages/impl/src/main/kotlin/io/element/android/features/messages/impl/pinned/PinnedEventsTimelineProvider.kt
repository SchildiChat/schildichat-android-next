/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.pinned

import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.coroutine.mapState
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.room.CreateTimelineParams
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.sync.SyncService
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.api.timeline.TimelineProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import javax.inject.Inject

@SingleIn(RoomScope::class)
class PinnedEventsTimelineProvider @Inject constructor(
    private val room: JoinedRoom,
    private val syncService: SyncService,
    private val featureFlagService: FeatureFlagService,
    private val dispatchers: CoroutineDispatchers,
) : TimelineProvider {
    private val _timelineStateFlow: MutableStateFlow<AsyncData<Timeline>> =
        MutableStateFlow(AsyncData.Uninitialized)

    override fun activeTimelineFlow(): StateFlow<Timeline?> {
        return _timelineStateFlow
            .mapState { value ->
                value.dataOrNull()
            }
    }

    val timelineStateFlow = _timelineStateFlow

    fun launchIn(scope: CoroutineScope) {
        _timelineStateFlow.subscriptionCount
            .map { count -> count > 0 }
            .distinctUntilChanged()
            .onEach { isActive ->
                if (isActive) {
                    onActive()
                } else {
                    onInactive()
                }
            }
            .launchIn(scope)
            .invokeOnCompletion { timelineStateFlow.value.dataOrNull()?.close() }
    }

    private suspend fun onActive() = coroutineScope {
        combine(
            featureFlagService.isFeatureEnabledFlow(FeatureFlags.PinnedEvents),
            syncService.syncState,
        ) { isEnabled, _ ->
            // do not use syncState here as data can be loaded from cache, it's just to trigger retry if needed
            isEnabled
        }
            .onEach { isFeatureEnabled ->
                if (isFeatureEnabled) {
                    loadTimelineIfNeeded()
                } else {
                    resetTimeline()
                }
            }
            .launchIn(this)
    }

    private suspend fun onInactive() {
        resetTimeline()
    }

    private suspend fun resetTimeline() {
        invokeOnTimeline {
            close()
        }
        _timelineStateFlow.emit(AsyncData.Uninitialized)
    }

    suspend fun invokeOnTimeline(action: suspend Timeline.() -> Unit) {
        when (val asyncTimeline = timelineStateFlow.value) {
            is AsyncData.Success -> action(asyncTimeline.data)
            else -> Unit
        }
    }

    private suspend fun loadTimelineIfNeeded() {
        when (timelineStateFlow.value) {
            is AsyncData.Uninitialized, is AsyncData.Failure -> {
                timelineStateFlow.emit(AsyncData.Loading())
                withContext(dispatchers.io) {
                    room.createTimeline(CreateTimelineParams.PinnedOnly)
                }
                    .fold(
                        { timelineStateFlow.emit(AsyncData.Success(it)) },
                        { timelineStateFlow.emit(AsyncData.Failure(it)) }
                    )
            }
            else -> Unit
        }
    }
}
