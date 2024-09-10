/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.services.analyticsproviders.sentry

object SentryConfig {
    const val NAME = "Sentry"
    const val DNS = "localhost" // SC disable - SC-TODO is this enough?
    const val ENV_DEBUG = "DEBUG"
    const val ENV_RELEASE = "RELEASE"
}
