package com.lipengzhou.webtvlive

/** Explicit cross-app transport contract; no notification-listener permission is required. */
object GuardPlaybackContract {
    const val ACTION = "com.tonvchung.webtv.GUARD_PLAYBACK"
    const val GUARD_PACKAGE = "com.icybard.learningguard"
    const val MSG_GET_SESSION_TOKEN = 1
    const val MSG_SESSION_TOKEN = 2
    const val EXTRA_SESSION_TOKEN = "sessionToken"
}
