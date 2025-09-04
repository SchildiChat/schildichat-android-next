/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.x.oidc

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.AppScope
import io.element.android.libraries.matrix.api.auth.OidcRedirectUrlProvider
import io.element.android.services.toolbox.api.strings.StringProvider
import io.element.android.x.R
import dev.zacsweers.metro.Inject

@ContributesBinding(AppScope::class)
@Inject
class DefaultOidcRedirectUrlProvider(
    private val stringProvider: StringProvider,
) : OidcRedirectUrlProvider {
    override fun provide() = buildString {
        append(stringProvider.getString(R.string.login_redirect_scheme))
        append(":/")
    }
}
