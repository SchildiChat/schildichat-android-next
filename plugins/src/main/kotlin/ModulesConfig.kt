/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

import config.AnalyticsConfig
import config.PushProvidersConfig

object ModulesConfig {
    val pushProvidersConfig = PushProvidersConfig(
        includeFirebase = false,
        includeUnifiedPush = true,
    )

    val analyticsConfig: AnalyticsConfig = AnalyticsConfig.Disabled /* AnalyticsConfig.Enabled(
        withPosthog = true,
        withSentry = true,
    )*/
}
