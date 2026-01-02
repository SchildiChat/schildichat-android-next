/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.onboarding.classic

import android.content.ComponentName
import android.content.Context
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.core.log.logger.LoggerTag
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.core.meta.BuildType
import io.element.android.libraries.di.annotations.AppCoroutineScope
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.matrix.api.core.UserId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

interface ElementClassicConnection {
    fun start()
    fun stop()
    fun requestData()
    val state: StateFlow<ElementClassicConnectionState>
}

sealed interface ElementClassicConnectionState {
    object Idle : ElementClassicConnectionState
    object ElementClassicNotFound : ElementClassicConnectionState
    object ElementClassicReadyNoSession : ElementClassicConnectionState
    data class ElementClassicReady(val userId: UserId) : ElementClassicConnectionState
    data class Error(val error: String) : ElementClassicConnectionState
}

private val loggerTag = LoggerTag("ECConnection")

@ContributesBinding(AppScope::class)
class DefaultElementClassicConnection(
    @ApplicationContext
    private val context: Context,
    @AppCoroutineScope
    private val coroutineScope: CoroutineScope,
    private val buildMeta: BuildMeta,
) : ElementClassicConnection {
    // Messenger for communicating with the service.
    private var messenger: Messenger? = null

    // Target we publish for external service to send messages to IncomingHandler.
    private val incomingMessenger: Messenger = Messenger(IncomingHandler())

    // Flag indicating whether we have called bind on the service.
    private var bound: Boolean = false

    /**
     * Class for interacting with the main interface of the service.
     */
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            Timber.tag(loggerTag.value).d("onServiceConnected")
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service. We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            messenger = Messenger(service)
            bound = true
            // Request the data as soon as possible
            requestData()
        }

        override fun onServiceDisconnected(className: ComponentName) {
            Timber.tag(loggerTag.value).d("onServiceDisconnected")
            // This is called when the connection with the service has been
            // unexpectedly disconnected&mdash;that is, its process crashed.
            messenger = null
            bound = false
        }
    }

    override fun start() {
        Timber.tag(loggerTag.value).w("start()")
        coroutineScope.launch {
            // Establish a connection with the service. We use an explicit
            // class name because there is no reason to be able to let other
            // applications replace our component.
            try {
                val intentService = Intent()
                intentService.setComponent(getElementClassicComponent(buildMeta))
                if (context.bindService(intentService, serviceConnection, BIND_AUTO_CREATE)) {
                    Timber.tag(loggerTag.value).d("Binding returned true")
                } else {
                    // This happen when the app is not installed
                    Timber.tag(loggerTag.value).d("Binding returned false")
                    elementClassicConnectionStateFlow.emit(ElementClassicConnectionState.ElementClassicNotFound)
                }
            } catch (e: SecurityException) {
                Timber.tag(loggerTag.value).e(e, "Can't bind to Service")
                elementClassicConnectionStateFlow.emit(ElementClassicConnectionState.Error(e.localizedMessage.orEmpty()))
            }
        }
    }

    override fun stop() {
        Timber.tag(loggerTag.value).w("stop(): Unbinding (bound=$bound)")
        if (bound) {
            // Detach our existing connection.
            context.unbindService(serviceConnection)
            bound = false
        }
        coroutineScope.launch {
            elementClassicConnectionStateFlow.emit(ElementClassicConnectionState.Idle)
        }
    }

    override fun requestData() {
        Timber.tag(loggerTag.value).w("requestData()")
        coroutineScope.launch {
            val finalMessenger = messenger
            if (finalMessenger == null) {
                Timber.tag(loggerTag.value).w("The messenger is null, can't request data")
                elementClassicConnectionStateFlow.emit(ElementClassicConnectionState.Error("The messenger is null, can't request data"))
            } else {
                try {
                    // Get the data
                    val msg = Message.obtain(null, MSG_GET_DATA)
                    msg.replyTo = incomingMessenger
                    finalMessenger.send(msg)
                } catch (e: RemoteException) {
                    // In this case the service has crashed before we could even
                    // do anything with it; we can count on soon being
                    // disconnected (and then reconnected if it can be restarted)
                    // so there is no need to do anything here.
                    Timber.tag(loggerTag.value).e(e, "RemoteException")
                    elementClassicConnectionStateFlow.emit(ElementClassicConnectionState.Error(e.localizedMessage.orEmpty()))
                }
            }
        }
    }

    private val elementClassicConnectionStateFlow = MutableStateFlow<ElementClassicConnectionState>(ElementClassicConnectionState.Idle)

    override val state: StateFlow<ElementClassicConnectionState>
        get() = elementClassicConnectionStateFlow.asStateFlow()

    /**
     * Handler of incoming messages from service.
     */
    @Suppress("DEPRECATION")
    inner class IncomingHandler : Handler() {
        override fun handleMessage(msg: Message) {
            Timber.tag(loggerTag.value).d("IncomingHandler handling message ${msg.what}")
            when (msg.what) {
                MSG_GET_DATA -> {
                    // The data must be extracted from the bundle before we launch the coroutine, else the bundle will be emptied
                    val state = msg.data.toElementClassicConnectionState()
                    emitElementClassicState(state)
                }
                else -> {
                    super.handleMessage(msg)
                }
            }
        }
    }

    private fun emitElementClassicState(state: ElementClassicConnectionState) = coroutineScope.launch {
        when (state) {
            is ElementClassicConnectionState.Error -> {
                Timber.tag(loggerTag.value).w("Received error from Element Classic: %s", state.error)
                elementClassicConnectionStateFlow.emit(state)
            }
            is ElementClassicConnectionState.ElementClassicReady -> {
                Timber.tag(loggerTag.value).d("Received userId from Element Classic: %s", state.userId)
                elementClassicConnectionStateFlow.emit(state)
            }
            ElementClassicConnectionState.ElementClassicReadyNoSession -> {
                Timber.tag(loggerTag.value).d("Received no session from Element Classic")
                elementClassicConnectionStateFlow.emit(state)
            }
            else -> {
                // Should not happen
                Timber.tag(loggerTag.value).w("Received unexpected state from Element Classic: %s", state)
                elementClassicConnectionStateFlow.emit(ElementClassicConnectionState.Idle)
            }
        }
    }

    private fun getElementClassicComponent(buildMeta: BuildMeta) = ComponentName(
        buildString {
            append(ELEMENT_CLASSIC_APP_ID)
            append(
                when (buildMeta.buildType) {
                    BuildType.DEBUG -> ELEMENT_CLASSIC_APP_ID_DEBUG_SUFFIX
                    BuildType.NIGHTLY -> ELEMENT_CLASSIC_APP_ID_NIGHTLY_SUFFIX
                    BuildType.RELEASE -> ELEMENT_CLASSIC_APP_ID_RELEASE_SUFFIX
                }
            )
        },
        ELEMENT_CLASSIC_SERVICE_FULL_CLASS_NAME,
    )

    private fun Bundle?.toElementClassicConnectionState(): ElementClassicConnectionState {
        return if (this == null) {
            ElementClassicConnectionState.Error("No data received from Element Classic")
        } else {
            val error = getString(KEY_ERROR_STR)
            if (error != null) {
                ElementClassicConnectionState.Error(error)
            } else {
                val userId = getString(KEY_USER_ID_STR)?.let(::UserId)
                if (userId != null) {
                    ElementClassicConnectionState.ElementClassicReady(userId)
                } else {
                    ElementClassicConnectionState.ElementClassicReadyNoSession
                }
            }
        }
    }

    // Everything in this companion object must match what is defined in Element Classic
    private companion object {
        // Command to the service to get the data.
        const val MSG_GET_DATA = 1

        const val ELEMENT_CLASSIC_APP_ID = "im.vector.app"
        const val ELEMENT_CLASSIC_APP_ID_DEBUG_SUFFIX = ".debug"
        const val ELEMENT_CLASSIC_APP_ID_NIGHTLY_SUFFIX = ".nightly"
        const val ELEMENT_CLASSIC_APP_ID_RELEASE_SUFFIX = ""

        const val ELEMENT_CLASSIC_SERVICE_FULL_CLASS_NAME = "im.vector.app.features.importer.ImporterService"

        // Keys for the bundle returned from the service
        const val KEY_ERROR_STR = "error"
        const val KEY_USER_ID_STR = "userId"
    }
}
