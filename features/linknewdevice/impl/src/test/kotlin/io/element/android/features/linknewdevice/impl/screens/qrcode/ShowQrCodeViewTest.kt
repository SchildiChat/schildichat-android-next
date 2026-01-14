/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.linknewdevice.impl.screens.qrcode

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.pressBackKey
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShowQrCodeViewTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `on back pressed - calls the expected callback`() {
        ensureCalledOnce { callback ->
            rule.setView(
                onBackClick = callback
            )
            rule.pressBackKey()
        }
    }

    private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setView(
        onBackClick: () -> Unit = EnsureNeverCalled(),
    ) {
        setContent {
            ShowQrCodeView(
                data = "DATA",
                onBackClick = onBackClick,
            )
        }
    }
}
