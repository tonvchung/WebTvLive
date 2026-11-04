package com.lipengzhou.webtvlive

import android.os.SystemClock

/** Monotonic, mode-aware double-back handling for live playback and official replay. */
class BackPressController(
    private val doublePressIntervalMs: Long = DOUBLE_PRESS_INTERVAL_MS,
    private val clock: () -> Long = SystemClock::elapsedRealtime,
) {
    enum class Action {
        SHOW_REPLAY_HINT,
        SHOW_EXIT_HINT,
        RETURN_TO_LIVE,
        EXIT_APP,
    }

    private data class FirstPress(val atMillis: Long, val replayActive: Boolean)

    private var firstPress: FirstPress? = null

    fun onBack(isReplayActive: Boolean): Action {
        val now = clock()
        val previous = firstPress
        val isSecondPress = previous != null &&
            previous.replayActive == isReplayActive &&
            now >= previous.atMillis &&
            now - previous.atMillis < doublePressIntervalMs

        if (isSecondPress) {
            firstPress = null
            return if (isReplayActive) Action.RETURN_TO_LIVE else Action.EXIT_APP
        }

        firstPress = FirstPress(now, isReplayActive)
        return if (isReplayActive) Action.SHOW_REPLAY_HINT else Action.SHOW_EXIT_HINT
    }

    private companion object {
        const val DOUBLE_PRESS_INTERVAL_MS = 2_000L
    }
}
