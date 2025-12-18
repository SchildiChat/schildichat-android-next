/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.crypto.historyvisible

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.appconfig.LearnMoreConfig
import io.element.android.libraries.designsystem.atomic.molecules.ComposerAlertLevel
import io.element.android.libraries.designsystem.atomic.molecules.ComposerAlertMolecule
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.text.stringWithLink
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun HistoryVisibleStateView(
    state: HistoryVisibleState,
    onLinkClick: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!state.showAlert) {
        return
    }
    ComposerAlertMolecule(
        modifier = modifier,
        avatar = null,
        showIcon = true,
        level = ComposerAlertLevel.Info,
        content = stringWithLink(
            textRes = CommonStrings.crypto_history_visible,
            url = LearnMoreConfig.HISTORY_VISIBLE_URL,
            onLinkClick = { url -> onLinkClick(url, true) },
        ),
        submitText = stringResource(CommonStrings.action_dismiss),
        onSubmitClick = { state.eventSink(HistoryVisibleEvent.Acknowledge) },
    )
}

@PreviewsDayNight
@Composable
internal fun HistoryVisibleStateViewPreview(
    @PreviewParameter(HistoryVisibleStateProvider::class) state: HistoryVisibleState,
) = ElementPreview {
    HistoryVisibleStateView(
        state = state,
        onLinkClick = { _, _ -> },
    )
}
