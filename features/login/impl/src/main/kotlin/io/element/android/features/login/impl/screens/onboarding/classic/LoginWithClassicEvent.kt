/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.onboarding.classic

sealed interface LoginWithClassicEvent {
    data object RefreshData : LoginWithClassicEvent
    data object StartLoginWithClassic : LoginWithClassicEvent
    data object DoLoginWithClassic : LoginWithClassicEvent
    data object CloseDialog : LoginWithClassicEvent
}
