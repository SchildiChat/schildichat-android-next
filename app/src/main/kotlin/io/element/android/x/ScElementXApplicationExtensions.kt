package io.element.android.x

import android.content.Context
import androidx.startup.Initializer
import io.element.android.libraries.architecture.bindings
import io.element.android.x.di.AppBindings
import kotlinx.coroutines.runBlocking
import timber.log.Timber

class ScInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        val ts = System.currentTimeMillis()
        val appBindings = context.bindings<AppBindings>()
        // runBlocking is not nice, but we want to make sure that preferences are ready before creating room list service
        runBlocking {
            appBindings.scPreferencesStore().prefetch()
        }
        Timber.d("Initialized SC dependencies in ${System.currentTimeMillis() - ts} ms")
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
