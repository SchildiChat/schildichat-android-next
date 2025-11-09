package io.element.android.x

import android.app.Application
import android.util.Log
import io.element.android.libraries.di.DependencyInjectionGraphOwner

abstract class ScApplication : Application(), DependencyInjectionGraphOwner {
    // Timber not yet initialized in ScApplication onCreate
    @Suppress("LogNotTimber")
    override fun onCreate() {
        super.onCreate()
        Log.v("SchildiNext", "Launch ${BuildConfig.APPLICATION_ID}, OIDC scheme is ${getString(R.string.login_redirect_scheme)} (upstream: ${getString(R.string.login_redirect_scheme_upstream)})")
    }
}
