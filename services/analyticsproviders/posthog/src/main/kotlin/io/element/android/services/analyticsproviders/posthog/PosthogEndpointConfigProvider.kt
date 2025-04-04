/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.analyticsproviders.posthog

import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.core.meta.BuildType
import javax.inject.Inject

class PosthogEndpointConfigProvider @Inject constructor(
    private val buildMeta: BuildMeta,
) {
    fun provide(): PosthogEndpointConfig {
        return when (buildMeta.buildType) {
            // Disable analytics for SC builds
            BuildType.RELEASE_SC,
            BuildType.DEBUG_SC -> PosthogEndpointConfig(
                host = "localhost",
                apiKey = "",
            )
            BuildType.RELEASE -> PosthogEndpointConfig(
                host = "https://posthog.element.io",
                apiKey = "phc_Jzsm6DTm6V2705zeU5dcNvQDlonOR68XvX2sh1sEOHO",
            )
            BuildType.NIGHTLY,
            BuildType.DEBUG -> PosthogEndpointConfig(
                host = "https://posthog.element.dev",
                apiKey = "phc_VtA1L35nw3aeAtHIx1ayrGdzGkss7k1xINeXcoIQzXN",
            )
        }
    }
}
