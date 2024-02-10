package io.element.android.features.messages.impl.timeline.components.event

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.core.graphics.drawable.toBitmap
import coil.compose.LocalImageLoader
import coil.request.ImageRequest
import io.element.android.libraries.matrix.api.media.MediaSource
import kotlinx.coroutines.runBlocking

@Composable
fun shouldDrawScPreviewMedia(mediaSource: MediaSource): Boolean {
    return LocalInspectionMode.current && mediaSource.url.startsWith("mxc://")
}

@Composable
fun ScPreviewMedia(mediaSource: MediaSource, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val imageLoader = LocalImageLoader.current
    val drawable = runBlocking { imageLoader.execute(ImageRequest.Builder(context).data(mediaSource).build()) }.drawable ?: return
    Box(modifier) {
        Image(drawable.toBitmap().asImageBitmap(), contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())
    }
}
