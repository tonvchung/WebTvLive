package com.lipengzhou.webtvlive

import org.junit.Assert.assertEquals
import org.junit.Test

class ReplayEligibilityTest {
    private val cctv = Channel("CCTV-13 新闻", "CCTV13", "600001811")
    private val satellite = Channel("湖南卫视", "湖南卫视", "600002475")
    private val ended = ProgramGuideItem(
        programId = "program",
        name = "已播节目",
        startEpochSeconds = 100,
        endEpochSeconds = 200,
        startTime = "00:01",
        endTime = "00:03",
        durationSeconds = 100,
        isVip = false,
        copyrightFlag = "1",
        timeShiftReviewFlag = "1",
    )

    @Test
    fun onlyFinishedPubliclyReplayableCctvProgramsAreAvailable() {
        assertEquals(
            ReplayEligibility.Reason.AVAILABLE,
            ReplayEligibility.evaluate(cctv, ended, 200),
        )
        assertEquals(
            ReplayEligibility.Reason.CHANNEL_UNSUPPORTED,
            ReplayEligibility.evaluate(satellite, ended, 200),
        )
        assertEquals(
            ReplayEligibility.Reason.NOT_ENDED,
            ReplayEligibility.evaluate(cctv, ended, 199),
        )
    }

    @Test
    fun rightsAndMembershipRestrictionsAreNeverBypassed() {
        assertEquals(
            ReplayEligibility.Reason.RIGHTS_RESTRICTED,
            ReplayEligibility.evaluate(cctv, ended.copy(isVip = true), 200),
        )
        assertEquals(
            ReplayEligibility.Reason.RIGHTS_RESTRICTED,
            ReplayEligibility.evaluate(cctv, ended.copy(copyrightFlag = "0"), 200),
        )
        assertEquals(
            ReplayEligibility.Reason.RIGHTS_RESTRICTED,
            ReplayEligibility.evaluate(cctv, ended.copy(timeShiftReviewFlag = "0"), 200),
        )
    }
}
