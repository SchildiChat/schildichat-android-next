/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.linknewdevice.impl.screens.error

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.pressBackKey
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ErrorViewTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `on back pressed - calls the onCancel callback`() {
        ensureCalledOnce { callback ->
            rule.setErrorView(
                onCancel = callback,
            )
            rule.pressBackKey()
        }
    }

    @Test
    fun `on try again button clicked - calls the expected callback`() {
        ensureCalledOnce { callback ->
            rule.setErrorView(
                onRetry = callback
            )
            rule.clickOn(CommonStrings.action_try_again)
        }
    }

    @Test
    fun `on cancel button clicked - calls the expected callback`() {
        ensureCalledOnce { callback ->
            rule.setErrorView(
                onCancel = callback
            )
            rule.clickOn(CommonStrings.action_cancel)
        }
    }

    private fun <R : TestRule> AndroidComposeTestRule<R, ComponentActivity>.setErrorView(
        onRetry: () -> Unit = EnsureNeverCalled(),
        onCancel: () -> Unit = EnsureNeverCalled(),
        errorScreenType: ErrorScreenType = ErrorScreenType.UnknownError,
    ) {
        setContent {
            ErrorView(
                errorScreenType = errorScreenType,
                onRetry = onRetry,
                onCancel = onCancel,
            )
        }
    }
}
