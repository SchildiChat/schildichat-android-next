package chat.schildi.screenshots

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import chat.schildi.lib.preferences.AbstractScPref
import chat.schildi.lib.preferences.LocalScPreferencesStore
import chat.schildi.lib.preferences.ScPref
import chat.schildi.lib.preferences.ScPreferencesStore
import chat.schildi.lib.preferences.ScPrefs
import chat.schildi.screenshots.data.getScScreenshotImageLoader
import coil.compose.LocalImageLoader
import io.element.android.libraries.designsystem.preview.DAY_MODE_NAME
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.NIGHT_MODE_NAME
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

val SC_SCREENSHOT_MXC_DRAWABLES = mapOf(
    "mxc://bob" to R.drawable.bob,
    "mxc://cake" to R.drawable.cake,
    "mxc://duck" to R.drawable.duck,
    "mxc://electric" to R.drawable.electric,
    "mxc://family" to R.drawable.family,
    "mxc://female" to R.drawable.female,
    "mxc://flying" to R.drawable.flying,
    "mxc://friends" to R.drawable.friends,
    "mxc://male" to R.drawable.male,
    "mxc://real" to R.drawable.real,
    "mxc://ring" to R.drawable.ring,
    "mxc://matrix" to R.drawable.matrix,
    "mxc://test" to R.drawable.test,
    "mxc://valkyrie" to R.drawable.valkyrie,
    "mxc://wotan" to R.drawable.wotan,
)

private val SC_SCREENSHOT_PREFS = mapOf<String, Any>(
    ScPrefs.SPACE_NAV.sKey to true,
)

@Preview(
    name = DAY_MODE_NAME,
    fontScale = 1f,
    widthDp = 420,
    heightDp = 780,
)
@Preview(
    name = NIGHT_MODE_NAME,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    fontScale = 1f,
    widthDp = 420,
    heightDp = 780,
)
annotation class ScScreenshot

@Composable
fun ScScreenshot(content: @Composable () -> Unit) {
    ElementPreview {
        val context = LocalContext.current
        CompositionLocalProvider(
            LocalImageLoader provides remember { getScScreenshotImageLoader(context) },
            LocalInspectionMode provides true, // Android-Studio sets this one automatically for previews, but not all tools to extract screenshots may do so
            LocalScPreferencesStore provides remember { ScreenshotScPreferencesStore(SC_SCREENSHOT_PREFS) },
            content = content
        )
    }
}

class ScreenshotScPreferencesStore(private val prefOverrides: Map<String, Any>) : ScPreferencesStore {
    override suspend fun <T> setSetting(scPref: ScPref<T>, value: T) {}
    override suspend fun <T> setSettingTypesafe(scPref: ScPref<T>, value: Any?) {}
    override fun <T> settingFlow(scPref: ScPref<T>): Flow<T> = flowOf(scPref.ensureType(prefOverrides[scPref.sKey]) ?: scPref.defaultValue)
    override fun isEnabledFlow(scPref: AbstractScPref): Flow<Boolean> = flowOf(true)
    override fun <T> getCachedOrDefaultValue(scPref: ScPref<T>): T = scPref.ensureType(prefOverrides[scPref.sKey]) ?: scPref.defaultValue
    override suspend fun reset() {}
    override suspend fun prefetch() {}
}
