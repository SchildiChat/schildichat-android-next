/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.onboarding.classic

import io.element.android.tests.testutils.lambda.lambdaError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeElementClassicConnection(
    private val startResult: () -> Unit = { lambdaError() },
    private val stopResult: () -> Unit = { lambdaError() },
    private val requestDataResult: () -> Unit = { lambdaError() },
    initialState: ElementClassicConnectionState = ElementClassicConnectionState.Idle
) : ElementClassicConnection {
    override fun start() = startResult()
    override fun stop() = stopResult()
    override fun requestData() = requestDataResult()
    private val _state = MutableStateFlow(initialState)
    override val state: StateFlow<ElementClassicConnectionState> = _state.asStateFlow()
    suspend fun emitState(state: ElementClassicConnectionState) {
        _state.emit(state)
    }
}
