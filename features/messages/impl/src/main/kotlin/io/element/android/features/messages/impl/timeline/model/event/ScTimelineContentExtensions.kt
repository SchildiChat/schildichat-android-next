package io.element.android.features.messages.impl.timeline.model.event

import io.element.android.features.messages.impl.timeline.components.TimestampPosition

// MSC2530 helpers, not in original files to reduce upstream merge conflicts
fun TimelineItemAudioContent.filenameOrBody() = filename ?: body
fun TimelineItemAudioContent.caption() = body.takeIf { filename != null && filename != it }
fun TimelineItemFileContent.filenameOrBody() = filename ?: body
fun TimelineItemFileContent.caption() = body.takeIf { filename != null && filename != it }
fun TimelineItemImageContent.filenameOrBody() = filename ?: body
fun TimelineItemImageContent.caption() = body.takeIf { filename != null && filename != it }
fun TimelineItemVideoContent.filenameOrBody() = filename ?: body
fun TimelineItemVideoContent.caption() = body.takeIf { filename != null && filename != it }
fun TimelineItemVoiceContent.filenameOrBody() = filename ?: body
fun TimelineItemVoiceContent.caption() = body.takeIf { filename != null && filename != it }

fun TimelineItemImageContent.timestampPosition() = if (caption() == null) TimestampPosition.Overlay else TimestampPosition.Aligned
fun TimelineItemVideoContent.timestampPosition() = if (caption() == null) TimestampPosition.Overlay else TimestampPosition.Aligned
