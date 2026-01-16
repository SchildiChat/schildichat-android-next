/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.onboarding.classic

import io.element.android.libraries.architecture.AsyncAction

data class LoginWithClassicState(
    val canLoginWithClassic: Boolean,
    val loginWithClassicAction: AsyncAction<Unit>,
    val eventSink: (LoginWithClassicEvent) -> Unit,
)
