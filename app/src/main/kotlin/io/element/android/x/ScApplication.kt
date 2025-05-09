package io.element.android.x

import android.app.Application
import android.content.Context
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import io.element.android.libraries.architecture.bindings
import io.element.android.libraries.di.DaggerComponentOwner
import io.element.android.libraries.pushproviders.unifiedpush.ScPushWorker
import io.element.android.libraries.pushproviders.unifiedpush.UnifiedPushParser
import io.element.android.x.di.AppBindings

abstract class ScApplication : Application(), Configuration.Provider, DaggerComponentOwner {
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(ScWorkerFactory())
            .build()
}

class ScWorkerFactory : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        val appBindings = appContext.bindings<AppBindings>()
        return when (workerClassName) {
            ScPushWorker::class.java.name -> ScPushWorker(appContext, workerParameters, UnifiedPushParser(), appBindings.pushHandler())
            else -> null
        }
    }
}
