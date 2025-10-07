/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.labs

import io.element.android.libraries.featureflag.ui.model.FeatureUiModel
import kotlinx.collections.immutable.ImmutableList

data class LabsState(
    val features: ImmutableList<FeatureUiModel>,
    val isApplyingChanges: Boolean,
    val eventSink: (LabsEvents) -> Unit,
)
