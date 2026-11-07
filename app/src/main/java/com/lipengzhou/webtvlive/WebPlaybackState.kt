package com.lipengzhou.webtvlive

/** Actual state reported by the page's active HTML video element. */
enum class WebPlaybackState(val wireValue: String) {
    PLAYING("playing"),
    PAUSED("paused"),
    WAITING("waiting"),
    ENDED("ended"),
    ERROR("error"),
    ;

    companion object {
        fun fromWire(value: String): WebPlaybackState? = entries.firstOrNull { it.wireValue == value }
    }
}
