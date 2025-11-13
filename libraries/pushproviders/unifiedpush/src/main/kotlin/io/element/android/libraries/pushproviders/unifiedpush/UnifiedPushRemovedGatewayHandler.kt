/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.core.extensions.flatMap
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.push.api.PushService
import io.element.android.libraries.pushstore.api.clientsecret.PushClientSecret
import timber.log.Timber

private val loggerTag = LoggerTag("UnifiedPushRemovedGatewayHandler", LoggerTag.PushLoggerTag)

/**
 * Handle endpoint removal received from UnifiedPush. Will try to register again.
 */
fun interface UnifiedPushRemovedGatewayHandler {
    suspend fun handle(clientSecret: String): Result<Unit>
}

@ContributesBinding(AppScope::class)
class DefaultUnifiedPushRemovedGatewayHandler(
    private val unregisterUnifiedPushUseCase: UnregisterUnifiedPushUseCase,
    private val pushClientSecret: PushClientSecret,
    private val matrixClientProvider: MatrixClientProvider,
    private val pushService: PushService,
) : UnifiedPushRemovedGatewayHandler {
    override suspend fun handle(clientSecret: String): Result<Unit> {
        // Unregister the pusher for the session with this client secret.
        val userId = pushClientSecret.getUserIdFromSecret(clientSecret) ?: return Result.failure<Unit>(
            IllegalStateException("Unable to retrieve session")
        ).also {
            Timber.tag(loggerTag.value).w("Unable to retrieve session")
        }
        return matrixClientProvider
            .getOrRestore(userId)
            .onFailure {
                Timber.tag(loggerTag.value).w(it, "Fails to restore client")
            }
            .flatMap { client ->
                unregisterUnifiedPushUseCase.unregister(
                    matrixClient = client,
                    clientSecret = clientSecret,
                    unregisterUnifiedPush = false,
                )
                    .onFailure {
                        Timber.tag(loggerTag.value).w(it, "Unable to unregister pusher")
                    }
                    .flatMap {
                        val pushProvider = pushService.getCurrentPushProvider(userId)
                        val distributor = pushProvider?.getCurrentDistributor(userId)
                        // Attempt to register again
                        if (pushProvider != null && distributor != null) {
                            pushService.registerWith(
                                client,
                                pushProvider,
                                distributor,
                            )
                                .onFailure {
                                    Timber.tag(loggerTag.value).w(it, "Unable to register with current data")
                                }
                        } else {
                            Result.failure(IllegalStateException("Unable to register again"))
                        }
                    }
                    .onFailure {
                        // Let the user know
                        pushService.onServiceUnregistered(userId)
                    }
            }
            .onFailure {
                Timber.tag(loggerTag.value).w(it, "Issue during pusher unregistration / re registration")
            }
    }
}
