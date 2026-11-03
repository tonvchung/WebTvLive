package com.lipengzhou.webtvlive

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TvCatalogTest {
    @Test
    fun pageUrl_pointsToOfficialChannelPage() {
        val cctv13 = TvCatalog.flatChannels.first { it.siteName == "CCTV13" }

        assertEquals(
            "https://www.yangshipin.cn/tv/home?pid=600001811",
            TvCatalog.pageUrl(cctv13),
        )
    }

    @Test
    fun replayUrlUsesOfficialPageAndSpecialCctvSlugs() {
        val item = ProgramGuideItem(
            programId = "program",
            name = "已播节目",
            startEpochSeconds = 1793714400,
            endEpochSeconds = 1793716200,
            startTime = "22:00",
            endTime = "22:30",
            durationSeconds = 1800,
            isVip = false,
            copyrightFlag = "1",
            timeShiftReviewFlag = "1",
        )
        assertEquals(
            "https://tv.cctv.com/live/cctv13/?webtvReplayStart=1793714400&webtvRequestId=42",
            TvCatalog.replayPageUrl(TvCatalog.flatChannels.first { it.siteName == "CCTV13" }, item, 42),
        )
        assertEquals(
            "https://tv.cctv.com/live/cctv5plus/?webtvReplayStart=1793714400&webtvRequestId=42",
            TvCatalog.replayPageUrl(TvCatalog.flatChannels.first { it.siteName == "CCTV5+" }, item, 42),
        )
        assertEquals(
            "https://tv.cctv.com/live/cctv16/?webtvReplayStart=1793714400&webtvRequestId=42",
            TvCatalog.replayPageUrl(
                TvCatalog.flatChannels.first { it.siteName == "CCTV16-HD" },
                item,
                42,
            ),
        )
        assertEquals(
            null,
            TvCatalog.replayPageUrl(TvCatalog.flatChannels.first { it.siteName == "湖南卫视" }, item, 42),
        )
    }

    @Test
    fun everyChannelHasUniqueSiteNameAndPid() {
        val channels = TvCatalog.flatChannels

        assertTrue(channels.all { it.siteName.isNotBlank() && it.pid.isNotBlank() })
        assertEquals(channels.size, channels.map { it.siteName }.toSet().size)
        assertEquals(channels.size, channels.map { it.pid }.toSet().size)
    }

    @Test
    fun indexOfSiteName_findsStableFallback() {
        val index = TvCatalog.indexOfSiteName("CCTV9")

        assertTrue(index in TvCatalog.flatChannels.indices)
        assertEquals("CCTV-9 纪录", TvCatalog.flatChannels[index].name)
    }
}
