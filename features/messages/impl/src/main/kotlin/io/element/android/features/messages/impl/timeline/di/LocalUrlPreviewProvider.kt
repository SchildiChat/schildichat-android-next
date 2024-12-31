package io.element.android.features.messages.impl.timeline.di

import androidx.compose.runtime.staticCompositionLocalOf
import chat.schildi.matrixsdk.urlpreview.UrlPreviewStateProvider

/**
 * Provides a [UrlPreviewStateProvider] to the composition.
 */
val LocalUrlPreviewStateProvider = staticCompositionLocalOf<UrlPreviewStateProvider?> { null }
