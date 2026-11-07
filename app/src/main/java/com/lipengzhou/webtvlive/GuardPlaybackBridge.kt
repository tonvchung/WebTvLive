package com.lipengzhou.webtvlive

import android.content.Context
import android.media.session.MediaSession
import android.media.session.PlaybackState

/** Owns the standard MediaSession shared with the explicitly bound learning-guard client. */
object GuardPlaybackBridge {
    data class TransportListener(
        val onPlay: () -> Unit,
        val onPause: () -> Unit,
    )

    private var session: MediaSession? = null

    @Synchronized
    fun attach(context: Context, listener: TransportListener): MediaSession.Token {
        val activeSession = session ?: MediaSession(context, TAG).also { created ->
            created.setCallback(object : MediaSession.Callback() {
                override fun onPlay() = listener.onPlay()
                override fun onPause() = listener.onPause()
            })
            created.isActive = true
            session = created
        }
        activeSession.setPlaybackState(state(WebPlaybackState.PAUSED))
        return activeSession.sessionToken
    }

    @Synchronized
    fun sessionToken(): MediaSession.Token? = session?.sessionToken

    fun update(state: WebPlaybackState) {
        val activeSession = synchronized(this) { session } ?: return
        activeSession.setPlaybackState(state(state))
    }

    @Synchronized
    fun detach() {
        session?.release()
        session = null
    }

    private fun state(state: WebPlaybackState): PlaybackState = PlaybackState.Builder()
        .setActions(PlaybackState.ACTION_PLAY or PlaybackState.ACTION_PAUSE)
        .setState(
            when (state) {
                WebPlaybackState.PLAYING -> PlaybackState.STATE_PLAYING
                WebPlaybackState.PAUSED -> PlaybackState.STATE_PAUSED
                WebPlaybackState.WAITING -> PlaybackState.STATE_BUFFERING
                WebPlaybackState.ENDED -> PlaybackState.STATE_STOPPED
                WebPlaybackState.ERROR -> PlaybackState.STATE_ERROR
            },
            PlaybackState.PLAYBACK_POSITION_UNKNOWN,
            1f,
        )
        .build()

    private const val TAG = "WebTvLiveGuard"
}
