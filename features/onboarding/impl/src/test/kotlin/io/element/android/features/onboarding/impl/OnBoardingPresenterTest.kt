/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.onboarding.impl

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.appconfig.OnBoardingConfig
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class OnBoardingPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val buildMeta = aBuildMeta(
            applicationName = "A",
            productionApplicationName = "B",
            desktopApplicationName = "C",
        )
        val featureFlagService = FakeFeatureFlagService(
            initialState = mapOf(FeatureFlags.QrCodeLogin.key to true),
            buildMeta = buildMeta,
        )
        val presenter = OnBoardingPresenter(
            buildMeta = buildMeta,
            featureFlagService = featureFlagService,
            rageshakeFeatureAvailability = { true },
        )
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            assertThat(initialState.canLoginWithQrCode).isFalse()
            assertThat(initialState.productionApplicationName).isEqualTo("B")
            assertThat(initialState.canCreateAccount).isEqualTo(OnBoardingConfig.CAN_CREATE_ACCOUNT)
            assertThat(initialState.canReportBug).isTrue()
            assertThat(awaitItem().canLoginWithQrCode).isTrue()
        }
    }

    @Test
    fun `present - rageshake not available`() = runTest {
        val presenter = OnBoardingPresenter(
            buildMeta = aBuildMeta(),
            featureFlagService = FakeFeatureFlagService(),
            rageshakeFeatureAvailability = { false },
        )
        presenter.test {
            skipItems(1)
            assertThat(awaitItem().canReportBug).isFalse()
        }
    }
}
