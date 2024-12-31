package chat.schildi.matrixsdk.urlpreview

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

// Allow at most one server query every 5 minutes (unless recreating the state holder, e.g. when opening a new chat)
private const val PREVIEW_RE_QUERY_THROTTLE = 5 * 60 * 1_000

class UrlPreviewStateHolder(
    val url: String,
    private val urlPreviewProvider: UrlPreviewProvider,
    private val scope: CoroutineScope,
    dbgId: String,
) {
    private var lastQueryTs: Long = 0
    private val currentJob = AtomicReference<Job?>(null)
    private val _state = MutableStateFlow<UrlPreview?>(null)
    val state = _state.asStateFlow()

    private val logger = Timber.tag("UrlPreviewState/$dbgId")

    fun onRender() {
        // If we don't have a value for that and haven't tried within the last few minutes, launch a new lookup
        if (state.value == null && lastQueryTs + PREVIEW_RE_QUERY_THROTTLE < System.currentTimeMillis()) {
            launchLookup()
        } else {
            if (DEBUG_URL_PREVIEWS) logger.v("Skip re-lookup")
        }
    }

    private fun launchLookup() {
        // Only launch a query if we don't have one already running.
        currentJob.getAndUpdate {
            it?.takeIf { it.isActive } ?: scope.launch {
                logger.d("Launch lookup")
                lookup()
            }
        }
    }

    private suspend fun lookup() {
        urlPreviewProvider.fetchPreview(url) {
            _state.value = it
        }
    }
}

class UrlPreviewStateProvider(
    private val urlPreviewProvider: UrlPreviewProvider,
) {
    private val stateHolders = ConcurrentHashMap<String, UrlPreviewStateHolder>()
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        Timber.tag("UrlPreviewState").d("Init ${System.identityHashCode(this)}")
        // Debug lifecycle - need to make sure when closing a room, this gets gleaned up properly
        /*
        scope.launch {
            while (true) {
                Timber.tag("UrlPreviewState").v("${System.identityHashCode(this)} is alive")
                kotlinx.coroutines.delay(1000)
            }
        }
         */
    }

    fun getStateHolder(url: String): UrlPreviewStateHolder {
        return stateHolders.getOrPut(url) {
            UrlPreviewStateHolder(url, urlPreviewProvider, scope, "${System.identityHashCode(this)}/${stateHolders.size}")
        }
    }

    fun clear() {
        Timber.tag("UrlPreviewState").d("Clear ${System.identityHashCode(this)}")
        scope.cancel("State holder cleared")
        stateHolders.clear()
    }
}
