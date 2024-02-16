package io.element.android.libraries.core.meta

val BuildMeta.isInternalBuild: Boolean
    get() = this.isDebuggable || this.gitBranchName == "sm_fdroid"
val BuildMeta.minBugReportLength: Int
    get() = if (this.isInternalBuild) 3 else 10
