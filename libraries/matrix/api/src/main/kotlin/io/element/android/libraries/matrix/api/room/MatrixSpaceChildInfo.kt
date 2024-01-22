package io.element.android.libraries.matrix.api.room

import androidx.compose.runtime.Immutable

@Immutable
data class MatrixSpaceChildInfo(
    val roomId: String,
    val order: String?,
    val suggested: Boolean,
)
