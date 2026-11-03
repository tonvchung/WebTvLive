package com.lipengzhou.webtvlive

import java.net.URI

/** Main-frame navigation policy for pages allowed to communicate with the native app. */
object TrustedWebContent {
    fun isAllowedMainFrameUrl(rawUrl: String?): Boolean {
        if (rawUrl.isNullOrBlank()) return false
        val uri = runCatching { URI(rawUrl) }.getOrNull() ?: return false
        if (uri.scheme != "https" || uri.port != -1 || uri.userInfo != null) return false
        return when (uri.host) {
            YANGSHIPIN_HOST -> uri.path.trimEnd('/') == YANGSHIPIN_TV_PATH
            CCTV_HOST -> CCTV_REPLAY_PATH.matches(uri.path)
            else -> false
        }
    }

    private const val YANGSHIPIN_HOST = "www.yangshipin.cn"
    private const val YANGSHIPIN_TV_PATH = "/tv/home"
    private const val CCTV_HOST = "tv.cctv.com"
    private val CCTV_REPLAY_PATH = Regex("/live/cctv(?:[1-9]|1[0-7]|5plus)/?")
}
