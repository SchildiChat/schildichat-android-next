/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.reporter

fun interface BugReporterMultipartBodyListener {
    /**
     * Upload listener.
     *
     * @param totalWritten  total written bytes
     * @param contentLength content length
     */
    fun onWrite(totalWritten: Long, contentLength: Long)
}
