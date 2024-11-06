/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.call.test

import io.element.android.features.call.api.CurrentCall
import io.element.android.features.call.api.CurrentCallObserver
import kotlinx.coroutines.flow.MutableStateFlow

class FakeCurrentCallObserver(
    initialValue: CurrentCall = CurrentCall.None,
) : CurrentCallObserver {
    override val currentCall = MutableStateFlow(initialValue)

    fun setCurrentCall(value: CurrentCall) {
        currentCall.value = value
    }
}
