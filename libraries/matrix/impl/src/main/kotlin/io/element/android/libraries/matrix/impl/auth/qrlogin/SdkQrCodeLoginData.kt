/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.auth.qrlogin

import io.element.android.libraries.matrix.api.auth.qrlogin.MatrixQrCodeLoginData
import org.matrix.rustcomponents.sdk.QrCodeData as RustQrCodeData

class SdkQrCodeLoginData(
    internal val rustQrCodeData: RustQrCodeData,
) : MatrixQrCodeLoginData {
    override fun serverName(): String? {
        return rustQrCodeData.serverName()
    }
}
