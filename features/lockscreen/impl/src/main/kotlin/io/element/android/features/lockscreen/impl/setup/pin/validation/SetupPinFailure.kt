/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl.setup.pin.validation

sealed interface SetupPinFailure {
    data object ForbiddenPin : SetupPinFailure
    data object PinsDoNotMatch : SetupPinFailure
}
