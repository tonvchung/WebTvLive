package com.lipengzhou.webtvlive

import android.app.Service
import android.content.Intent
import android.media.session.MediaSession
import android.os.Binder
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.os.Parcel
import android.util.Log

/** Exposes only the MediaSession token to the reviewed learning-guard package UID. */
class GuardPlaybackService : Service() {
    private val messenger = Messenger(IncomingHandler())
    private val checkedBinder = object : Binder() {
        override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
            val callerPackages = packageManager.getPackagesForUid(Binder.getCallingUid()).orEmpty()
            if (GuardPlaybackContract.GUARD_PACKAGE !in callerPackages) {
                Log.w(TAG, "Rejected unapproved guard transaction")
                throw SecurityException("Learning Guard package required")
            }
            return messenger.binder.transact(code, data, reply, flags)
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        if (intent.action != GuardPlaybackContract.ACTION) return null
        if (GuardPlaybackBridge.sessionToken() == null) return null
        return checkedBinder
    }

    private class IncomingHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(message: Message) {
            if (message.what != GuardPlaybackContract.MSG_GET_SESSION_TOKEN) return
            val token = GuardPlaybackBridge.sessionToken() ?: return
            val reply = Message.obtain(null, GuardPlaybackContract.MSG_SESSION_TOKEN).apply {
                data = Bundle().apply {
                    classLoader = MediaSession.Token::class.java.classLoader
                    putParcelable(GuardPlaybackContract.EXTRA_SESSION_TOKEN, token)
                }
            }
            runCatching { message.replyTo?.send(reply) }
                .onFailure { Log.w(TAG, "Unable to return guard session token", it) }
        }
    }

    private companion object {
        const val TAG = "WebTvLiveGuard"
    }
}
