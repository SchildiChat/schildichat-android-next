package chat.schildi.matrixsdk.urlpreview

import android.content.Context
import dev.zacsweers.metro.Inject
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.matrix.api.MatrixClient
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import timber.log.Timber
import java.io.File
import java.io.InputStream
import java.security.MessageDigest
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

sealed interface UrlPreviewLookup

data class UrlPreviewInfo(
    val url: String,
    val preview: UrlPreview,
)

@Serializable
data class UrlPreview(
    @SerialName("og:image")
    val imageUrl: String? = null,
    @SerialName("og:title")
    val title: String? = null,
    @SerialName("og:description")
    val description: String? = null,
    @SerialName("og:site_name")
    val siteName: String? = null,
) : UrlPreviewLookup

@Serializable
private data class UrlPreviewFileCache(
    val url: String,
    val ts: Long,
    val data: String,
)

private const val PREVIEW_CACHE_LIFETIME_MILLIS = 7L * 24 * 60 * 60 * 1_000
const val DEBUG_URL_PREVIEWS = true

@Inject
class UrlPreviewProvider(
    @ApplicationContext
    private val context: Context,
    private val client: MatrixClient
) {
    private val coder = Json { ignoreUnknownKeys = true }
    private val logger = Timber.tag("UrlPreview")
    private val fileCacheLock = ReentrantLock()

    private val cacheDir = File(context.cacheDir, "url_previews")

    private fun getPreviewCacheFile(url: String): File = File(
        cacheDir,
        MessageDigest.getInstance("SHA-256").digest(url.toByteArray()).joinToString { "%02x".format(it) }
    )

    @OptIn(ExperimentalSerializationApi::class)
    private fun fetchPreviewFromFileCache(file: File): UrlPreviewFileCache? = fileCacheLock.withLock {
        if (file.exists()) {
            var stream: InputStream? = null
            return try {
                stream = file.inputStream()
                coder.decodeFromStream<UrlPreviewFileCache>(stream)
            } catch (t: Throwable) {
                logger.e(t, "Failed to read persisted url preview from file cache")
                null
            } finally {
                try {
                    stream?.close()
                } catch (t: Throwable) {
                    logger.w(t, "Failed to close stream")
                }
            }
        }
        return null
    }

    private fun persistPreviewToFileCache(file: File, url: String, previewData: String) = fileCacheLock.withLock {
        try {
            file.parentFile?.mkdirs() ?: kotlin.run {
                logger.e("Failed to create url preview directory")
                return
            }
            val cacheEntry = UrlPreviewFileCache(
                url = url,
                ts = System.currentTimeMillis(),
                data = previewData
            )
            val data = coder.encodeToString(cacheEntry)
            file.writeText(data, Charsets.UTF_8)
        } catch (t: Throwable) {
            logger.e(t, "Failed to persist url preview")
        }
    }

    suspend fun fetchPreview(url: String, publishPreview: (UrlPreview?) -> Unit) {
        val cacheFile = getPreviewCacheFile(url)
        val previewFromCache = fetchPreviewFromFileCache(cacheFile)
        if (previewFromCache != null) {
            val preview = previewFromCache.data.decodeUrlPreview()
            publishPreview(preview)
            if (previewFromCache.url == url && previewFromCache.ts + PREVIEW_CACHE_LIFETIME_MILLIS > System.currentTimeMillis()) {
                if (DEBUG_URL_PREVIEWS) logger.d("Cache hit for ts=${previewFromCache.ts}")
                // No need to re-lookup
                return
            } else {
                if (DEBUG_URL_PREVIEWS) logger.d("Preliminary cache hit for ts=${previewFromCache.ts}, url match=${previewFromCache.url == url}")
            }
        }
        val previewData = try {
            client.getUrlPreviewJson(url)
        } catch (e: CancellationException) {
            // Happens when scope is cancelled, e.g. composable moved out of screen
            logger.i("Cancelled fetching url preview")
            return
        } catch (t: Throwable) {
            // Don't print the error message, it can leak the url
            logger.w("Failed to fetch url preview: ${t.javaClass.canonicalName}")
            return
        }
        val preview = previewData.decodeUrlPreview()
        if (DEBUG_URL_PREVIEWS) logger.d("Fetched new url preview, decoded: ${preview != null}")
        publishPreview(preview)
        persistPreviewToFileCache(cacheFile, url, previewData)
    }

    private fun String.decodeUrlPreview(): UrlPreview? {
        return try {
            coder.decodeFromString<UrlPreview>(this)
        } catch (t: Throwable) {
            Timber.tag("UrlPreviewProvider").e(t, "Failed to decode url preview")
            null
        }
    }
}
