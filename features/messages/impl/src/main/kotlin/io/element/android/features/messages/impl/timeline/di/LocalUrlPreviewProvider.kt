package io.element.android.features.messages.impl.timeline.di

import androidx.compose.runtime.staticCompositionLocalOf
import chat.schildi.matrixsdk.urlpreview.UrlPreviewProvider

/**
 * Provides a [UrlPreviewProvider] to the composition.
 */
val LocalUrlPreviewProvider = staticCompositionLocalOf<UrlPreviewProvider?> { null }
