/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.verifysession.impl.outgoing

sealed interface VerifySelfSessionViewEvents {
    data object RequestVerification : VerifySelfSessionViewEvents
    data object StartSasVerification : VerifySelfSessionViewEvents
    data object ConfirmVerification : VerifySelfSessionViewEvents
    data object DeclineVerification : VerifySelfSessionViewEvents
    data object Cancel : VerifySelfSessionViewEvents
    data object Reset : VerifySelfSessionViewEvents
}
