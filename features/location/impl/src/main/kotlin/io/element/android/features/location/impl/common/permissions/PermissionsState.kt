/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.common.permissions

data class PermissionsState(
    val permissions: Permissions,
    val shouldShowRationale: Boolean,
    val eventSink: (PermissionsEvents) -> Unit,
) {
    sealed interface Permissions {
        data object AllGranted : Permissions
        data object SomeGranted : Permissions
        data object NoneGranted : Permissions
    }

    val isAnyGranted: Boolean
        get() = permissions is Permissions.SomeGranted || permissions is Permissions.AllGranted
}
