/*
 * Copyright 2021-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.about

import androidx.annotation.StringRes
import io.element.android.features.preferences.impl.BuildConfig
import io.element.android.libraries.ui.strings.CommonStrings

/*
private const val COPYRIGHT_URL = BuildConfig.URL_COPYRIGHT
private const val USE_POLICY_URL = BuildConfig.URL_ACCEPTABLE_USE
private const val PRIVACY_URL = BuildConfig.URL_PRIVACY
*/
private const val COPYRIGHT_URL = "https://element.io/copyright"
private const val USE_POLICY_URL = "https://element.io/acceptable-use-policy-terms"
private const val PRIVACY_URL = "https://schildi.chat/android/next/privacy"

sealed class ElementLegal(
    @StringRes val titleRes: Int,
    val url: String,
) {
    data object Copyright : ElementLegal(CommonStrings.common_copyright, COPYRIGHT_URL)
    data object AcceptableUsePolicy : ElementLegal(CommonStrings.common_acceptable_use_policy, USE_POLICY_URL)
    data object PrivacyPolicy : ElementLegal(CommonStrings.common_privacy_policy, PRIVACY_URL)
}

fun getAllLegals(): List<ElementLegal> {
    return listOf(
        ElementLegal.Copyright,
        ElementLegal.AcceptableUsePolicy,
        ElementLegal.PrivacyPolicy,
    )
}
