/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.linknewdevice.impl.screens.root

import com.google.common.truth.Truth.assertThat
import io.element.android.features.linknewdevice.impl.LinkNewMobileHandler
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class LinkNewDeviceRootPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - initial state`() = runTest {
        val matrixClient = FakeMatrixClient(
            canLinkNewDeviceResult = { Result.success(true) }
        )
        createPresenter(
            matrixClient = matrixClient,
        ).test {
            val initialState = awaitItem()
            assertThat(initialState.isSupported.isUninitialized()).isTrue()
            assertThat(awaitItem().isSupported.dataOrNull()).isTrue()
        }
    }

    @Test
    fun `present - new login device not supported`() = runTest {
        val matrixClient = FakeMatrixClient(
            canLinkNewDeviceResult = { Result.success(false) }
        )
        createPresenter(
            matrixClient = matrixClient,
        ).test {
            val initialState = awaitItem()
            assertThat(initialState.isSupported.isUninitialized()).isTrue()
            assertThat(awaitItem().isSupported.dataOrNull()).isFalse()
        }
    }

    @Test
    fun `present - error`() = runTest {
        val matrixClient = FakeMatrixClient(
            canLinkNewDeviceResult = { Result.failure(AN_EXCEPTION) }
        )
        createPresenter(
            matrixClient = matrixClient,
        ).test {
            val initialState = awaitItem()
            assertThat(initialState.isSupported.isUninitialized()).isTrue()
            assertThat(awaitItem().isSupported.isFailure()).isTrue()
        }
    }

    private fun createPresenter(
        matrixClient: MatrixClient = FakeMatrixClient(),
        linkNewMobileHandler: LinkNewMobileHandler = LinkNewMobileHandler(matrixClient),
    ) = LinkNewDeviceRootPresenter(
        matrixClient = matrixClient,
        linkNewMobileHandler = linkNewMobileHandler,
    )
}
