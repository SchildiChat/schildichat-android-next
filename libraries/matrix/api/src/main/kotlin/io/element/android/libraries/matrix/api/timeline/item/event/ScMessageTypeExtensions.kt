package io.element.android.libraries.matrix.api.timeline.item.event

// Message type to caption - keep in sync with ScTimelineContentExtensions.kt!
fun AudioMessageType.caption() = body.takeIf { filename != null && filename != it }
fun FileMessageType.caption() = body.takeIf { filename != null && filename != it }
fun ImageMessageType.caption() = body.takeIf { filename != null && filename != it }
fun VideoMessageType.caption() = body.takeIf { filename != null && filename != it }
fun VoiceMessageType.caption() = body.takeIf { filename != null && filename != it }

fun MessageType.caption(): String? {
    return when (this) {
        is AudioMessageType -> caption()
        is VoiceMessageType -> caption()
        is FileMessageType -> caption()
        is ImageMessageType -> caption()
        is VideoMessageType -> caption()
        else -> null
    }
}
