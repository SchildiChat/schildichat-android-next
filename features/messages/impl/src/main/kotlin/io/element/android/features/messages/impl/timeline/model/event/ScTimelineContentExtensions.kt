package io.element.android.features.messages.impl.timeline.model.event

import io.element.android.features.messages.impl.timeline.components.TimestampPosition

// MSC2530 helpers, not in original files to reduce upstream merge conflicts

// Timeline item to caption - keep in sync with ScMessageType.kt!
fun TimelineItemAudioContent.caption() = body.takeIf { filename != null && filename != it }
fun TimelineItemFileContent.caption() = body.takeIf { filename != null && filename != it }
fun TimelineItemImageContent.caption() = body.takeIf { filename != null && filename != it }
fun TimelineItemVideoContent.caption() = body.takeIf { filename != null && filename != it }
fun TimelineItemVoiceContent.caption() = body.takeIf { filename != null && filename != it }

// TODO use these instead of body if upstream assumes somewhere that body = filename
/*
fun TimelineItemAudioContent.filenameOrBody() = filename ?: body
fun TimelineItemFileContent.filenameOrBody() = filename ?: body
fun TimelineItemImageContent.filenameOrBody() = filename ?: body
fun TimelineItemVideoContent.filenameOrBody() = filename ?: body
fun TimelineItemVoiceContent.filenameOrBody() = filename ?: body
 */

// Timeline item timestamp position depending on whether we have a caption or not
fun TimelineItemImageContent.timestampPosition() = if (caption() == null) TimestampPosition.Overlay else TimestampPosition.Aligned
fun TimelineItemVideoContent.timestampPosition() = if (caption() == null) TimestampPosition.Overlay else TimestampPosition.Aligned
