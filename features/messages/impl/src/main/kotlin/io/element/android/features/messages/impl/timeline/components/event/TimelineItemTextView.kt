/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.messages.impl.timeline.components.event

import android.text.SpannableString
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewParameter
import chat.schildi.lib.compose.thenIf
import chat.schildi.lib.preferences.ScPrefs.EL_TYPOGRAPHY
import chat.schildi.lib.preferences.ScPrefs.SC_TIMELINE_LAYOUT
import chat.schildi.lib.preferences.value
import chat.schildi.matrixsdk.containsOnlyEmojis
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.messages.impl.timeline.components.layout.ContentAvoidingLayout
import io.element.android.features.messages.impl.timeline.components.layout.ContentAvoidingLayoutData
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextBasedContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextBasedContentProvider
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.textcomposer.ElementRichTextEditorStyle
import io.element.android.wysiwyg.compose.EditorStyledText

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimelineItemTextView(
    content: TimelineItemTextBasedContent,
    onLinkClicked: (String) -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    onContentLayoutChanged: (ContentAvoidingLayoutData) -> Unit = {},
) {
    val canCollapse = content.formattedCollapsedBody != null
    val emojiOnly = !canCollapse && SC_TIMELINE_LAYOUT.value() &&
        (content.formattedBody == null || content.formattedBody.toString() == content.body) &&
        content.body.replace(" ", "").containsOnlyEmojis(50)
    val textStyle = when {
        emojiOnly -> ElementTheme.typography.fontHeadingXlBold
        EL_TYPOGRAPHY.value() -> ElementTheme.typography.fontBodyLgRegular
        else -> ElementTheme.typography.fontBodyMdRegular
    }
    CompositionLocalProvider(
        LocalContentColor provides ElementTheme.colors.textPrimary,
        LocalTextStyle provides textStyle,
    ) {
        var collapsed by remember(canCollapse) { mutableStateOf(canCollapse) }
        val formattedBody = if (collapsed && canCollapse) content.formattedCollapsedBody else content.formattedBody
        val body = SpannableString(formattedBody ?: content.body)

        Box(modifier.semantics { contentDescription = body.toString() }
            .thenIf(canCollapse) { combinedClickable(onClick = { collapsed = !collapsed }, onLongClick = onLongClick) }
        ) {
            EditorStyledText(
                text = body,
                onLinkClickedListener = onLinkClicked,
                style = ElementRichTextEditorStyle.textStyle(),
                onTextLayout = ContentAvoidingLayout.measureLegacyLastTextLine(onContentLayoutChanged = onContentLayoutChanged),
                releaseOnDetach = false,
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun TimelineItemTextViewPreview(
    @PreviewParameter(TimelineItemTextBasedContentProvider::class) content: TimelineItemTextBasedContent
) = ElementPreview {
    TimelineItemTextView(
        content = content,
        onLinkClicked = {},
        onLongClick = {},
    )
}
