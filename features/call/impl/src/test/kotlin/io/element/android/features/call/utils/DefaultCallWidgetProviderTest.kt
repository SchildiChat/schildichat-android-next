/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.utils

import com.google.common.truth.Truth.assertThat
import io.element.android.features.call.impl.utils.DefaultCallWidgetProvider
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.call.ElementCallBaseUrlProvider
import io.element.android.libraries.matrix.api.widget.CallWidgetSettingsProvider
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.FakeMatrixClientProvider
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.libraries.matrix.test.widget.FakeCallWidgetSettingsProvider
import io.element.android.libraries.matrix.test.widget.FakeMatrixWidgetDriver
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import io.element.android.libraries.preferences.test.InMemoryAppPreferencesStore
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.lambda.value
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultCallWidgetProviderTest {
    @Test
    fun `getWidget - fails if the session does not exist`() = runTest {
        val provider = createProvider(matrixClientProvider = FakeMatrixClientProvider { Result.failure(Exception("Session not found")) })
        assertThat(provider.getWidget(A_SESSION_ID, A_ROOM_ID, "clientId", "languageTag", "theme").isFailure).isTrue()
    }

    @Test
    fun `getWidget - fails if the room does not exist`() = runTest {
        val client = FakeMatrixClient().apply {
            givenGetRoomResult(A_ROOM_ID, null)
        }
        val provider = createProvider(matrixClientProvider = FakeMatrixClientProvider { Result.success(client) })
        assertThat(provider.getWidget(A_SESSION_ID, A_ROOM_ID, "clientId", "languageTag", "theme").isFailure).isTrue()
    }

    @Test
    fun `getWidget - fails if it can't generate the URL for the widget`() = runTest {
        val room = FakeMatrixRoom(
            generateWidgetWebViewUrlResult = { _, _, _, _ -> Result.failure(Exception("Can't generate URL for widget")) }
        )
        val client = FakeMatrixClient().apply {
            givenGetRoomResult(A_ROOM_ID, room)
        }
        val provider = createProvider(matrixClientProvider = FakeMatrixClientProvider { Result.success(client) })
        assertThat(provider.getWidget(A_SESSION_ID, A_ROOM_ID, "clientId", "languageTag", "theme").isFailure).isTrue()
    }

    @Test
    fun `getWidget - fails if it can't get the widget driver`() = runTest {
        val room = FakeMatrixRoom(
            generateWidgetWebViewUrlResult = { _, _, _, _ -> Result.success("url") },
            getWidgetDriverResult = { Result.failure(Exception("Can't get a widget driver")) }
        )
        val client = FakeMatrixClient().apply {
            givenGetRoomResult(A_ROOM_ID, room)
        }
        val provider = createProvider(matrixClientProvider = FakeMatrixClientProvider { Result.success(client) })
        assertThat(provider.getWidget(A_SESSION_ID, A_ROOM_ID, "clientId", "languageTag", "theme").isFailure).isTrue()
    }

    @Test
    fun `getWidget - returns a widget driver when all steps are successful`() = runTest {
        val room = FakeMatrixRoom(
            generateWidgetWebViewUrlResult = { _, _, _, _ -> Result.success("url") },
            getWidgetDriverResult = { Result.success(FakeMatrixWidgetDriver()) },
        )
        val client = FakeMatrixClient().apply {
            givenGetRoomResult(A_ROOM_ID, room)
        }
        val provider = createProvider(matrixClientProvider = FakeMatrixClientProvider { Result.success(client) })
        assertThat(provider.getWidget(A_SESSION_ID, A_ROOM_ID, "clientId", "languageTag", "theme").getOrNull()).isNotNull()
    }

    @Test
    fun `getWidget - will use a custom base url if it exists`() = runTest {
        val room = FakeMatrixRoom(
            generateWidgetWebViewUrlResult = { _, _, _, _ -> Result.success("url") },
            getWidgetDriverResult = { Result.success(FakeMatrixWidgetDriver()) },
        )
        val client = FakeMatrixClient().apply {
            givenGetRoomResult(A_ROOM_ID, room)
        }
        val preferencesStore = InMemoryAppPreferencesStore().apply {
            setCustomElementCallBaseUrl("https://custom.element.io")
        }
        val settingsProvider = FakeCallWidgetSettingsProvider()
        val provider = createProvider(
            matrixClientProvider = FakeMatrixClientProvider { Result.success(client) },
            callWidgetSettingsProvider = settingsProvider,
            appPreferencesStore = preferencesStore,
        )
        provider.getWidget(A_SESSION_ID, A_ROOM_ID, "clientId", "languageTag", "theme")

        assertThat(settingsProvider.providedBaseUrls).containsExactly("https://custom.element.io")
    }

    @Test
    fun `getWidget - will use a wellknown base url if it exists`() = runTest {
        val aCustomUrl = "https://custom.element.io"
        val providesLambda = lambdaRecorder<MatrixClient, String?> { _ -> aCustomUrl }
        val elementCallBaseUrlProvider = FakeElementCallBaseUrlProvider { matrixClient ->
            providesLambda(matrixClient)
        }
        val room = FakeMatrixRoom(
            generateWidgetWebViewUrlResult = { _, _, _, _ -> Result.success("url") },
            getWidgetDriverResult = { Result.success(FakeMatrixWidgetDriver()) },
        )
        val client = FakeMatrixClient().apply {
            givenGetRoomResult(A_ROOM_ID, room)
        }
        val settingsProvider = FakeCallWidgetSettingsProvider()
        val provider = createProvider(
            matrixClientProvider = FakeMatrixClientProvider { Result.success(client) },
            callWidgetSettingsProvider = settingsProvider,
            elementCallBaseUrlProvider = elementCallBaseUrlProvider,
        )
        provider.getWidget(A_SESSION_ID, A_ROOM_ID, "clientId", "languageTag", "theme")
        assertThat(settingsProvider.providedBaseUrls).containsExactly(aCustomUrl)
        providesLambda.assertions()
            .isCalledOnce()
            .with(value(client))
    }

    private fun createProvider(
        matrixClientProvider: MatrixClientProvider = FakeMatrixClientProvider(),
        appPreferencesStore: AppPreferencesStore = InMemoryAppPreferencesStore(),
        callWidgetSettingsProvider: CallWidgetSettingsProvider = FakeCallWidgetSettingsProvider(),
        elementCallBaseUrlProvider: ElementCallBaseUrlProvider = FakeElementCallBaseUrlProvider { _ -> null },
    ) = DefaultCallWidgetProvider(
        matrixClientsProvider = matrixClientProvider,
        appPreferencesStore = appPreferencesStore,
        callWidgetSettingsProvider = callWidgetSettingsProvider,
        elementCallBaseUrlProvider = elementCallBaseUrlProvider,
    )
}
