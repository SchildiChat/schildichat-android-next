/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securityandprivacy.impl.manageauthorizedspaces

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun ManageAuthorizedSpacesView(
    state: ManageAuthorizedSpacesState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            ManageAuthorizedSpacesTopBar(
                onBackClick = onBackClick,
                onDoneClick = {
                    state.eventSink(ManageAuthorizedSpacesEvent.Done)
                },
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding)
        ) {

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManageAuthorizedSpacesTopBar(
    onBackClick: () -> Unit,
    onDoneClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        titleStr = stringResource(CommonStrings.screen_manage_authorized_spaces_title),
        navigationIcon = { BackButton(onClick = onBackClick) },
        actions = {
            TextButton(
                text = stringResource(CommonStrings.action_done),
                onClick = onDoneClick,
            )
        }
    )
}

@PreviewsDayNight
@Composable
internal fun ManageAuthorizedSpacesViewPreview(
    @PreviewParameter(ManageAuthorizedSpacesStateProvider::class) state: ManageAuthorizedSpacesState
) = ElementPreview {
    ManageAuthorizedSpacesView(
        state = state,
        onBackClick = {},
    )
}
