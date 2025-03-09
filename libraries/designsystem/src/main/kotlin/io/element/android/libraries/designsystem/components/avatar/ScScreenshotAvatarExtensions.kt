package io.element.android.libraries.designsystem.components.avatar

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.core.graphics.drawable.toBitmap
import coil3.request.ImageRequest
import kotlinx.coroutines.runBlocking

@Composable
fun shouldDrawScPreviewAvatar(avatarData: AvatarData): Boolean {
    return LocalInspectionMode.current && avatarData.url?.startsWith("mxc://") == true
}

@Composable
fun ScPreviewAvatar(avatarData: AvatarData, modifier: Modifier) {
    val context = LocalContext.current
    // TODO
    //val imageLoader = LocalImageLoader.current
    //val drawable = runBlocking { imageLoader.execute(ImageRequest.Builder(context).data(avatarData).build()) }.drawable ?: return
    //Image(drawable.toBitmap().asImageBitmap(), contentDescription = null, contentScale = ContentScale.Crop, modifier = modifier)
}
