/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.permissions.api

import kotlinx.coroutines.flow.Flow

interface PermissionsStore {
    suspend fun setPermissionDenied(permission: String, value: Boolean)
    fun isPermissionDenied(permission: String): Flow<Boolean>

    suspend fun setPermissionAsked(permission: String, value: Boolean)
    fun isPermissionAsked(permission: String): Flow<Boolean>

    suspend fun resetPermission(permission: String)

    // To debug
    suspend fun resetStore()
}
