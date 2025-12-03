/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securityandprivacy.impl.manageauthorizedspaces

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.ui.room.address.RoomAddressValidity

open class ManageAuthorizedSpacesStateProvider : PreviewParameterProvider<ManageAuthorizedSpacesState> {
    override val values: Sequence<ManageAuthorizedSpacesState>
        get() = sequenceOf()
}

