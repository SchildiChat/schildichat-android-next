package chat.schildi.matrixsdk

data class ScTimelineFilterSettings(
    val showHiddenEvents: Boolean = false,
    val showRedactions: Boolean = true,
    val preferHideThreadedEvents: Boolean? = null,
    val showMembershipEventsInPublicRooms: Boolean = false,
) {
    companion object {
        val IncludeAll = ScTimelineFilterSettings(
            showHiddenEvents = true,
            showRedactions = true,
            preferHideThreadedEvents = false,
            showMembershipEventsInPublicRooms = true,
        )
    }
}
