package chat.schildi.screenshots.data

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.content.res.AppCompatResources
import chat.schildi.screenshots.SC_SCREENSHOT_MXC_DRAWABLES
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.test.FakeImageLoaderEngine
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.matrix.api.media.MediaSource

@OptIn(ExperimentalCoilApi::class)
fun getScScreenshotImageLoader(context: Context): ImageLoader {
    val drawables = SC_SCREENSHOT_MXC_DRAWABLES.mapValues { (_, resId) ->
        AppCompatResources.getDrawable(context, resId)
    }
    return ImageLoader.Builder(context)
        .components {
            val engine = FakeImageLoaderEngine.Builder()
            drawables.forEach { (model, drawable) ->
                engine.intercept(
                    predicate = {
                        it == model || (it as? AvatarData)?.url == model || (it as? MediaSource)?.url == model
                    },
                    drawable = drawable ?: ColorDrawable(Color.GREEN),
                )
            }
            add(engine.build())
        }
        .build()
}
