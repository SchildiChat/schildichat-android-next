package io.element.android.libraries.core.meta

val BuildMeta.isInternalBuild: Boolean
    get() = this.isDebuggable || this.gitBranchName == "sm_fdroid"
val BuildMeta.isScBetaBuild: Boolean
    get() = this.gitBranchName == "sc_testing_fdroid"
val BuildMeta.isGplayBuild: Boolean
    get() = "gplay" in this.gitBranchName
val BuildMeta.minBugReportLength: Int
    get() = if (this.isInternalBuild) 3 else 10
