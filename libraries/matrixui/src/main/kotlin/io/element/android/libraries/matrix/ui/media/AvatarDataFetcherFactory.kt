/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.media

import coil3.ImageLoader
import coil3.fetch.Fetcher
import coil3.request.Options
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.matrix.api.MatrixClient

internal class AvatarDataFetcherFactory(
    private val client: MatrixClient
) : Fetcher.Factory<AvatarData> {
    override fun create(
        data: AvatarData,
        options: Options,
        imageLoader: ImageLoader
    ): Fetcher {
        return CoilMediaFetcher(
            mediaLoader = client.mediaLoader,
            mediaData = data.toMediaRequestData(),
        )
    }
}
