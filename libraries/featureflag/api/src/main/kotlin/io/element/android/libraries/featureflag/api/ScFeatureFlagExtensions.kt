package io.element.android.libraries.featureflag.api

import android.os.Build

// On old devices, non-worker approach is more reliable
val SC_DEFAULT_ENABLE_NOTIFICATION_WORKER = Build.VERSION.SDK_INT >= 31
