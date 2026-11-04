package com.lipengzhou.webtvlive

import org.junit.Assert.assertEquals
import org.junit.Test

class BackPressControllerTest {
    private var now = 10_000L
    private val controller = BackPressController(clock = { now })

    @Test
    fun replayRequiresTwoQuickBackPresses() {
        assertEquals(
            BackPressController.Action.SHOW_REPLAY_HINT,
            controller.onBack(isReplayActive = true),
        )
        now += 500
        assertEquals(
            BackPressController.Action.RETURN_TO_LIVE,
            controller.onBack(isReplayActive = true),
        )
    }

    @Test
    fun liveStillRequiresTwoQuickBackPressesToExit() {
        assertEquals(
            BackPressController.Action.SHOW_EXIT_HINT,
            controller.onBack(isReplayActive = false),
        )
        now += 500
        assertEquals(
            BackPressController.Action.EXIT_APP,
            controller.onBack(isReplayActive = false),
        )
    }

    @Test
    fun expiredFirstPressOnlyShowsHintAgain() {
        controller.onBack(isReplayActive = true)
        now += 2_000
        assertEquals(
            BackPressController.Action.SHOW_REPLAY_HINT,
            controller.onBack(isReplayActive = true),
        )
    }

    @Test
    fun changingPlaybackModeCancelsPendingConfirmation() {
        controller.onBack(isReplayActive = true)
        now += 100
        assertEquals(
            BackPressController.Action.SHOW_EXIT_HINT,
            controller.onBack(isReplayActive = false),
        )
    }

    @Test
    fun successfulDoublePressDoesNotArmAnotherConfirmation() {
        controller.onBack(isReplayActive = true)
        now += 100
        controller.onBack(isReplayActive = true)
        now += 100
        assertEquals(
            BackPressController.Action.SHOW_EXIT_HINT,
            controller.onBack(isReplayActive = false),
        )
    }

    @Test
    fun backwardsClockAdjustmentCannotConfirmDoublePress() {
        controller.onBack(isReplayActive = true)
        now -= 100
        assertEquals(
            BackPressController.Action.SHOW_REPLAY_HINT,
            controller.onBack(isReplayActive = true),
        )
    }
}
