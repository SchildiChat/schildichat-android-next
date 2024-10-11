package chat.schildi.lib.util

fun formatUnreadCount(count: Long): String {
    return if (count > 1_000_000) {
        "%.1fM".format(count / 1_000_000f)
    } else if (count > 1_000) {
        "%.1fk".format(count / 1_000f)
    } else {
        count.toString()
    }
}
