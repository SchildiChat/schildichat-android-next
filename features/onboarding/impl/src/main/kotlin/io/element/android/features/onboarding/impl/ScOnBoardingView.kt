/*
 * Copyright (c) 2023 New Vector Ltd
 * Copyright (c) 2024 SchildiChat
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.onboarding.impl

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.atomic.atoms.ElementLogoAtomSize
import io.element.android.libraries.designsystem.atomic.atoms.ScLogoAtom
import io.element.android.libraries.designsystem.atomic.molecules.ButtonColumnMolecule
import io.element.android.libraries.designsystem.atomic.pages.OnBoardingPage
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.OutlinedButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.testtags.TestTags
import io.element.android.libraries.testtags.testTag
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun ScOnBoardingView(
    state: OnBoardingState,
    onSignInWithQrCode: () -> Unit,
    onSignIn: () -> Unit,
    onCreateAccount: () -> Unit,
    onReportProblem: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OnBoardingPage(
        modifier = modifier,
        content = {
            OnBoardingContent(state = state)
        },
        footer = {
            OnBoardingButtons(
                state = state,
                onSignInWithQrCode = onSignInWithQrCode,
                onSignIn = onSignIn,
                onCreateAccount = onCreateAccount,
                onReportProblem = onReportProblem,
            )
        }
    )
}

@Composable
private fun OnBoardingContent(
    state: OnBoardingState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = BiasAlignment(
                horizontalBias = 0f,
                verticalBias = -0.4f
            )
        ) {
            ScLogoAtom(
                size = ElementLogoAtomSize.Large,
                modifier = Modifier.padding(top = ElementLogoAtomSize.Large.shadowRadius / 2)
            )
        }
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = BiasAlignment(
                horizontalBias = 0f,
                verticalBias = 0.6f
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = CenterHorizontally,
            ) {
                Text(
                    text = stringResource(id = chat.schildi.lib.R.string.sc_onboarding_welcome_title),
                    color = ElementTheme.materialColors.primary,
                    style = ElementTheme.typography.fontHeadingLgBold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = chat.schildi.lib.R.string.sc_onboarding_welcome_summary),
                    color = ElementTheme.materialColors.secondary,
                    style = ElementTheme.typography.fontBodyLgRegular.copy(fontSize = 17.sp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun OnBoardingButtons(
    state: OnBoardingState,
    onSignInWithQrCode: () -> Unit,
    onSignIn: () -> Unit,
    onCreateAccount: () -> Unit,
    onReportProblem: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ButtonColumnMolecule(modifier = modifier) {
        val signInButtonStringRes = if (state.canLoginWithQrCode || state.canCreateAccount) {
            R.string.screen_onboarding_sign_in_manually
        } else {
            CommonStrings.action_continue
        }
        if (state.canLoginWithQrCode) {
            Button(
                text = stringResource(id = R.string.screen_onboarding_sign_in_with_qr_code),
                leadingIcon = IconSource.Vector(Icons.Default.QrCode),
                onClick = onSignInWithQrCode,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Button(
            text = stringResource(id = signInButtonStringRes),
            onClick = onSignIn,
            modifier = Modifier
                .fillMaxWidth()
                .testTag(TestTags.onBoardingSignIn)
        )
        if (state.canCreateAccount) {
            OutlinedButton(
                text = stringResource(id = R.string.screen_onboarding_sign_up),
                onClick = onCreateAccount,
                enabled = true,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Add a report problem text button. Use a Text since we need a special theme here.
        Text(
            modifier = Modifier
                .padding(8.dp)
                .clickable(onClick = onReportProblem),
            text = stringResource(id = CommonStrings.common_report_a_problem),
            style = ElementTheme.typography.fontBodySmRegular,
            color = ElementTheme.colors.textSecondary,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun ScOnBoardingScreenPreview(
    @PreviewParameter(OnBoardingStateProvider::class) state: OnBoardingState
) = ElementPreview {
    ScOnBoardingView(
        state = state,
        onSignInWithQrCode = {},
        onSignIn = {},
        onCreateAccount = {},
        onReportProblem = {},
    )
}
