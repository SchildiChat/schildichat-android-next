/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.media

import coil3.ImageLoader
import io.element.android.libraries.matrix.api.media.MatrixMediaLoader

class FakeLoggedInImageLoaderFactory(
    private val newImageLoaderLambda: (MatrixMediaLoader) -> ImageLoader
) : LoggedInImageLoaderFactory {
    override fun newImageLoader(matrixMediaLoader: MatrixMediaLoader): ImageLoader {
        return newImageLoaderLambda(matrixMediaLoader)
    }
}
