/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.impl.ui

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.viewinterop.AndroidView
import io.element.android.features.call.impl.R
import io.element.android.features.call.impl.pip.PictureInPictureEvent
import io.element.android.features.call.impl.pip.PictureInPictureState
import io.element.android.features.call.impl.pip.aPictureInPictureState
import io.element.android.features.call.impl.utils.InvalidAudioDeviceReason
import io.element.android.features.call.impl.utils.WebViewAudioManager
import io.element.android.features.call.impl.utils.WebViewPipController
import io.element.android.features.call.impl.utils.WebViewWidgetMessageInterceptor
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.components.ProgressDialog
import io.element.android.libraries.designsystem.components.dialogs.ErrorDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.ui.strings.CommonStrings
import timber.log.Timber

typealias RequestPermissionCallback = (Array<String>) -> Unit

interface CallScreenNavigator {
    fun close()
}

@Composable
internal fun CallScreenView(
    state: CallScreenState,
    pipState: PictureInPictureState,
    onConsoleMessage: (ConsoleMessage) -> Unit,
    requestPermissions: (Array<String>, RequestPermissionCallback) -> Unit,
    modifier: Modifier = Modifier,
) {
    var callWebView by remember { mutableStateOf<WebView?>(null) }

    fun handleBack(fromNative: Boolean = false) {
        when (CallScreenBackPressPolicy.resolve(supportPip = pipState.supportPip, hasWebView = callWebView != null, fromNative)) {
            CallScreenBackPressAction.EnterPictureInPicture ->
                pipState.eventSink(PictureInPictureEvent.EnterPictureInPicture)
            CallScreenBackPressAction.DispatchEscapeToWebView ->
                callWebView?.dispatchEscKeyEvent()
            null -> Timber.d("Back press with unsupported pip is a no-op")
        }
    }

    BackHandler {
        handleBack(fromNative = true)
    }
    if (state.webViewError != null) {
        ErrorDialog(
            content = buildString {
                append(stringResource(CommonStrings.error_unknown))
                state.webViewError.takeIf { it.isNotEmpty() }?.let { append("\n\n").append(it) }
            },
            onSubmit = { state.eventSink(CallScreenEvent.Hangup) },
        )
    } else {
        var webViewAudioManager by remember { mutableStateOf<WebViewAudioManager?>(null) }
        val coroutineScope = rememberCoroutineScope()

        var invalidAudioDeviceReason by remember { mutableStateOf<InvalidAudioDeviceReason?>(null) }
        invalidAudioDeviceReason?.let {
            InvalidAudioDeviceDialog(invalidAudioDeviceReason = it) {
                invalidAudioDeviceReason = null
            }
        }

        Box(modifier = modifier.fillMaxSize()) {
            CallWebView(
                modifier = Modifier
                    .consumeWindowInsets(WindowInsets.systemBars)
                    .fillMaxSize(),
                url = state.urlState,
                userAgent = state.userAgent,
                onPermissionsRequest = { request ->
                    val androidPermissions = mapWebkitPermissions(request.resources)
                    val callback: RequestPermissionCallback = { request.grant(it) }
                    requestPermissions(androidPermissions.toTypedArray(), callback)
                },
                onConsoleMessage = onConsoleMessage,
                onCreateWebView = { webView ->
                    callWebView = webView
                    webView.addBackHandler(onBackPressed = ::handleBack)

                    // Native touch listener: every tap on the WebView resets the auto-hide timer.
                    // Returns false so the WebView processes the touch normally.
                    // This catches touches at the Android View level, before Element Call's JS
                    // can intercept them (Element Call suppresses click event synthesis).
                    webView.setOnTouchListener { _, event ->
                        if (event.action == MotionEvent.ACTION_UP) {
                            state.eventSink(CallScreenEvent.ScreenTapped)
                        }
                        false
                    }

                    val interceptor = WebViewWidgetMessageInterceptor(
                        webView = webView,
                        onUrlLoaded = { url ->
                            webView.evaluateJavascript("controls.onBackButtonPressed = () => { backHandler.onBackPressed() }", null)
                            if (webViewAudioManager?.isInCallMode?.get() == false) {
                                Timber.d("URL $url is loaded, starting in-call audio mode")
                                webViewAudioManager?.onCallStarted()
                            } else {
                                Timber.d("Can't start in-call audio mode since the app is already in it.")
                            }
                        },
                        onError = { state.eventSink(CallScreenEvent.OnWebViewError(it)) },
                    )
                    webViewAudioManager = WebViewAudioManager(
                        webView = webView,
                        coroutineScope = coroutineScope,
                        onInvalidAudioDeviceAdded = { invalidAudioDeviceReason = it },
                    )
                    state.eventSink(CallScreenEvent.SetupMessageChannels(interceptor))
                    val pipController = WebViewPipController(webView)
                    pipState.eventSink(PictureInPictureEvent.SetPipController(pipController))
                },
                onDestroyWebView = {
                    callWebView = null
                    webViewAudioManager?.onCallStopped()
                }
            )

            // Inject CSS to animate Element Call controls. The OnTouchListener on the WebView
            // (set up in onCreateWebView) handles tap detection for both showing controls
            // and resetting the auto-hide timer — without blocking touches from reaching
            // any visible elements underneath (menu items, buttons, etc.).
            LaunchedEffect(state.areControlsVisible, callWebView) {
                val webView = callWebView ?: return@LaunchedEffect
                val visible = state.areControlsVisible
                val js = """
                    (function() {
                        var VISIBLE = $visible;
                        var HIDE_CLASS = 'sc-controls-hidden';
                        var TOP_CLASS = 'sc-controls-top';
                        var BOTTOM_CLASS = 'sc-controls-bottom';

                        // Inject CSS once
                        if (!document.getElementById('sc-controls-style')) {
                            var style = document.createElement('style');
                            style.id = 'sc-controls-style';
                            style.textContent = [
                                '.sc-controls-hidden {',
                                '  opacity: 0 !important;',
                                '  pointer-events: none !important;',
                                '  transition: opacity 0.3s ease, transform 0.3s ease !important;',
                                '}',
                                '.sc-controls-top.sc-controls-hidden {',
                                '  transform: translateY(-100%) !important;',
                                '}',
                                '.sc-controls-bottom.sc-controls-hidden {',
                                '  transform: translateY(100%) !important;',
                                '}',
                                '.sc-controls-top, .sc-controls-bottom {',
                                '  transition: opacity 0.3s ease, transform 0.3s ease !important;',
                                '}',
                            ].join('\n');
                            document.head.appendChild(style);
                        }

                        // Only hide elements at screen edges (not mid-page menu/popup content)
                        function isNearTop(el) {
                            return el.getBoundingClientRect().top < 80;
                        }
                        function isNearBottom(el) {
                            return el.getBoundingClientRect().bottom > window.innerHeight - 80;
                        }

                        var topSelectors = [
                            'header', 'nav',
                            '[class*="topBar"]', '[class*="TopBar"]',
                            '[class*="headerBar"]', '[class*="HeaderBar"]',
                            '[class*="topControls"]', '[class*="TopControls"]',
                            '[class*="roomHeader"]', '[class*="RoomHeader"]',
                            '[class*="callHeader"]', '[class*="CallHeader"]',
                        ];

                        var bottomSelectors = [
                            '[class*="controlsBar"]', '[class*="ControlsBar"]',
                            '[class*="bottomBar"]', '[class*="BottomBar"]',
                            '[class*="callControls"]', '[class*="CallControls"]',
                            '[class*="controlsContainer"]', '[class*="ControlsContainer"]',
                            '[data-testid="call-controls"]',
                            '[class*="actionBar"]', '[class*="ActionBar"]',
                            '[class*="buttonBar"]', '[class*="ButtonBar"]',
                            '[class*="footer"]', '[class*="Footer"]',
                        ];

                        function toggleAll(selectors, cls, posCheck) {
                            for (var i = 0; i < selectors.length; i++) {
                                var els = document.querySelectorAll(selectors[i]);
                                for (var j = 0; j < els.length; j++) {
                                    var el = els[j];
                                    if (posCheck && !posCheck(el)) continue;
                                    if (VISIBLE) {
                                        el.classList.remove(HIDE_CLASS);
                                        el.classList.remove(cls);
                                    } else {
                                        el.classList.add(HIDE_CLASS);
                                        el.classList.add(cls);
                                    }
                                }
                            }
                        }

                        toggleAll(topSelectors, TOP_CLASS, isNearTop);
                        toggleAll(bottomSelectors, BOTTOM_CLASS, isNearBottom);

                        // Camera switch button in video tile, no position check
                        var cameraSelectors = [
                            '[class*="switchCamera"]', '[class*="SwitchCamera"]',
                        ];
                        toggleAll(cameraSelectors, BOTTOM_CLASS, null);

                        try {
                            if (typeof controls !== 'undefined' && controls.setControlsVisible) {
                                controls.setControlsVisible(VISIBLE);
                            }
                        } catch(e) {}
                    })();
                """.trimIndent()
                webView.evaluateJavascript(js, null)
            }

            when (state.urlState) {
                AsyncData.Uninitialized,
                is AsyncData.Loading ->
                    ProgressDialog(text = stringResource(id = CommonStrings.common_please_wait))
                is AsyncData.Failure -> {
                    Timber.e(state.urlState.error, "WebView failed to load URL: ${state.urlState.error.message}")
                    ErrorDialog(
                        content = state.urlState.error.message.orEmpty(),
                        onSubmit = { state.eventSink(CallScreenEvent.Hangup) },
                    )
                }
                is AsyncData.Success -> Unit
            }
        }
    }
}

@Composable
private fun InvalidAudioDeviceDialog(
    invalidAudioDeviceReason: InvalidAudioDeviceReason,
    onDismiss: () -> Unit,
) {
    ErrorDialog(
        content = when (invalidAudioDeviceReason) {
            InvalidAudioDeviceReason.BT_AUDIO_DEVICE_DISABLED -> {
                stringResource(R.string.call_invalid_audio_device_bluetooth_devices_disabled)
            }
        },
        onSubmit = onDismiss,
    )
}

@Composable
private fun CallWebView(
    url: AsyncData<String>,
    userAgent: String,
    onPermissionsRequest: (PermissionRequest) -> Unit,
    onConsoleMessage: (ConsoleMessage) -> Unit,
    onCreateWebView: (WebView) -> Unit,
    onDestroyWebView: (WebView) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (LocalInspectionMode.current) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("WebView - can't be previewed")
        }
    } else {
        AndroidView(
            modifier = modifier,
            factory = { context ->
                WebView(context).apply {
                    onCreateWebView(this)
                    setup(
                        userAgent = userAgent,
                        onPermissionsRequested = onPermissionsRequest,
                        onConsoleMessage = onConsoleMessage,
                    )
                }
            },
            update = { webView ->
                if (url is AsyncData.Success && webView.url != url.data) {
                    webView.loadUrl(url.data)
                }
            },
            onRelease = { webView ->
                onDestroyWebView(webView)
                webView.destroy()
            }
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
private fun WebView.setup(
    userAgent: String,
    onPermissionsRequested: (PermissionRequest) -> Unit,
    onConsoleMessage: (ConsoleMessage) -> Unit,
) {
    layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )

    with(settings) {
        javaScriptEnabled = true
        allowContentAccess = true
        allowFileAccess = true
        domStorageEnabled = true
        mediaPlaybackRequiresUserGesture = false
        @Suppress("DEPRECATION")
        databaseEnabled = true
        loadsImagesAutomatically = true
        userAgentString = userAgent
    }

    webChromeClient = object : WebChromeClient() {
        override fun onPermissionRequest(request: PermissionRequest) {
            onPermissionsRequested(request)
        }

        override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
            onConsoleMessage(consoleMessage)
            return true
        }
    }
}

private fun WebView.addBackHandler(onBackPressed: () -> Unit) {
    addJavascriptInterface(
        JavascriptBackHandlerBridge(callback = onBackPressed),
        "backHandler"
    )
}

private fun WebView.dispatchEscKeyEvent() {
    dispatchKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_ESCAPE))
    dispatchKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_ESCAPE))
}

@PreviewsDayNight
@Composable
internal fun CallScreenViewPreview(
    @PreviewParameter(CallScreenStateProvider::class) state: CallScreenState,
) = ElementPreview {
    CallScreenView(
        state = state,
        pipState = aPictureInPictureState(),
        requestPermissions = { _, _ -> },
        onConsoleMessage = {},
    )
}

@PreviewsDayNight
@Composable
internal fun InvalidAudioDeviceDialogPreview() = ElementPreview {
    InvalidAudioDeviceDialog(invalidAudioDeviceReason = InvalidAudioDeviceReason.BT_AUDIO_DEVICE_DISABLED) {}
}

internal class JavascriptBackHandlerBridge(
    private val callback: () -> Unit,
) {
    @JavascriptInterface
    fun onBackPressed() {
        callback()
    }
}
