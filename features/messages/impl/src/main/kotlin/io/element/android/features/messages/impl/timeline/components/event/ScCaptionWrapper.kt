package io.element.android.features.messages.impl.timeline.components.event

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.element.android.features.messages.impl.timeline.components.layout.ContentAvoidingLayoutData
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextContent

@Composable
fun ScCaptionWrapper(
    caption: String?,
    isEdited: Boolean,
    onContentLayoutChanged: (ContentAvoidingLayoutData) -> Unit,
    onLinkClicked: (url: String) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (Modifier) -> Unit,
) {
    if (caption == null) {
        content(modifier)
    } else {
        Column(Modifier) {
            content(Modifier)
            TimelineItemTextView(
                content = TimelineItemTextContent(
                    body = caption,
                    htmlDocument = null,
                    plainText = caption,
                    formattedBody = null,
                    isEdited = isEdited,
                ),
                onContentLayoutChanged = onContentLayoutChanged,
                onLinkClicked = onLinkClicked,
                onLongClick = {}, // onLongClick only required when formattedBody is set and contains a <details> tag
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
            )
        }
    }
}
