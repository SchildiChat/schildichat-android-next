/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.core.data.tryOrNull
import io.element.android.libraries.di.AppScope
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber
import java.net.URL
import javax.inject.Inject

interface UnifiedPushGatewayResolver {
    suspend fun getGateway(endpoint: String, previousGateway: String?): String
}

@ContributesBinding(AppScope::class)
class DefaultUnifiedPushGatewayResolver @Inject constructor(
    private val unifiedPushApiFactory: UnifiedPushApiFactory,
    private val coroutineDispatchers: CoroutineDispatchers,
) : UnifiedPushGatewayResolver {
    private val logger = Timber.tag("DefaultUnifiedPushGatewayResolver")

    override suspend fun getGateway(endpoint: String, previousGateway: String?): String {
        val url = tryOrNull(
            onError = { logger.d(it, "Cannot parse endpoint as an URL") }
        ) {
            URL(endpoint)
        }
        return if (url == null) {
            logger.d("Using default gateway")
            UnifiedPushConfig.DEFAULT_PUSH_GATEWAY_HTTP_URL
        } else {
            val port = if (url.port != -1) ":${url.port}" else ""
            val customBase = "${url.protocol}://${url.host}$port"
            val customUrl = "$customBase/_matrix/push/v1/notify"
            logger.i("Testing $customUrl")
            return withContext(coroutineDispatchers.io) {
                val api = unifiedPushApiFactory.create(customBase)
                try {
                    val discoveryResponse = api.discover()
                    if (discoveryResponse.unifiedpush.gateway == "matrix") {
                        logger.d("The endpoint seems to be a valid UnifiedPush gateway")
                        customUrl
                    } else {
                        // The endpoint returned a 200 OK but didn't promote an actual matrix gateway, which means it doesn't have any
                        logger.w("The endpoint does not seem to be a valid UnifiedPush gateway, using fallback")
                        UnifiedPushConfig.DEFAULT_PUSH_GATEWAY_HTTP_URL
                    }
                } catch (exception: HttpException) {
                    if (exception.code() == 404) {
                        logger.i("Checking for UnifiedPush endpoint yielded 404, using fallback")
                        UnifiedPushConfig.DEFAULT_PUSH_GATEWAY_HTTP_URL
                    } else {
                        logger.e(exception, "Error checking for UnifiedPush endpoint")
                        previousGateway ?: customUrl
                    }
                } catch (throwable: Throwable) {
                    logger.e(throwable, "Error checking for UnifiedPush endpoint")
                    previousGateway ?: customUrl
                }
            }
        }
    }
}
