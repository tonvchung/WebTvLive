package com.lipengzhou.webtvlive

/** Decides whether the official CCTV page explicitly offers replay for a finished program. */
object ReplayEligibility {
    enum class Reason {
        AVAILABLE,
        CHANNEL_UNSUPPORTED,
        NOT_ENDED,
        RIGHTS_RESTRICTED,
    }

    fun evaluate(
        channel: Channel,
        item: ProgramGuideItem,
        nowEpochSeconds: Long,
    ): Reason = when {
        TvCatalog.cctvReplaySlug(channel) == null -> Reason.CHANNEL_UNSUPPORTED
        nowEpochSeconds < item.endEpochSeconds -> Reason.NOT_ENDED
        item.isVip || item.copyrightFlag != "1" || item.timeShiftReviewFlag != "1" ->
            Reason.RIGHTS_RESTRICTED
        else -> Reason.AVAILABLE
    }
}
