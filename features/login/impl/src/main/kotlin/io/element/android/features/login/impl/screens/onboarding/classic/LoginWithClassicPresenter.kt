/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.onboarding.classic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import dev.zacsweers.metro.Inject
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.sessionstorage.api.SessionStore
import io.element.android.libraries.sessionstorage.api.toUserListFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Inject
class LoginWithClassicPresenter(
    private val elementClassicConnection: ElementClassicConnection,
    private val sessionStore: SessionStore,
    private val featureFlagService: FeatureFlagService,
) : Presenter<LoginWithClassicState> {
    @Composable
    override fun present(): LoginWithClassicState {
        val coroutineScope = rememberCoroutineScope()

        val isSignInWithClassicEnabled by remember {
            featureFlagService.isFeatureEnabledFlow(FeatureFlags.SignInWithClassic)
        }.collectAsState(initial = false)

        if (isSignInWithClassicEnabled) {
            DisposableEffect(Unit) {
                elementClassicConnection.start()
                onDispose {
                    elementClassicConnection.stop()
                }
            }
        }

        val state by elementClassicConnection.state.collectAsState()
        val loginWithClassicAction = remember { mutableStateOf<AsyncAction<Unit>>(AsyncAction.Uninitialized) }

        val existingSession by remember {
            sessionStore.sessionsFlow().toUserListFlow()
        }.collectAsState(emptyList())

        val canLoginWithClassic by remember {
            derivedStateOf {
                when (val finalState = state) {
                    is ElementClassicConnectionState.ElementClassicReady -> {
                        // Ensure there is no existing session with the same Id.
                        finalState.userId.value !in existingSession && isSignInWithClassicEnabled
                    }
                    else -> false
                }
            }
        }

        fun handleEvent(event: LoginWithClassicEvent) {
            when (event) {
                LoginWithClassicEvent.RefreshData -> {
                    elementClassicConnection.requestData()
                }
                LoginWithClassicEvent.StartLoginWithClassic -> {
                    val currentState = elementClassicConnection.state.value
                    if (currentState is ElementClassicConnectionState.ElementClassicReady) {
                        loginWithClassicAction.value = ConfirmingLoginWithElementClassic(
                            userId = currentState.userId,
                        )
                    } else {
                        loginWithClassicAction.value = AsyncAction.Failure(IllegalStateException("Element Classic is not ready"))
                    }
                }
                LoginWithClassicEvent.DoLoginWithClassic -> coroutineScope.launch {
                    // TODO Implement real login logic here
                    loginWithClassicAction.value = AsyncAction.Loading
                    delay(1000)
                    loginWithClassicAction.value = AsyncAction.Success(Unit)
                }
                LoginWithClassicEvent.CloseDialog -> {
                    loginWithClassicAction.value = AsyncAction.Uninitialized
                }
            }
        }

        return LoginWithClassicState(
            canLoginWithClassic = canLoginWithClassic,
            loginWithClassicAction = loginWithClassicAction.value,
            eventSink = ::handleEvent,
        )
    }
}
