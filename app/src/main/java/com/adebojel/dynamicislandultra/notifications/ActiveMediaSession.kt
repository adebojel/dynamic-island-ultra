package com.adebojel.dynamicislandultra.notifications

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.SystemClock

data class MediaSessionSnapshot(
    val packageName: String,
    val title: String,
    val artist: String,
    val album: String,
    val playing: Boolean,
    val positionMs: Long,
    val durationMs: Long,
    val artwork: Bitmap?
)

object ActiveMediaSession {
    const val ACTION_CHANGED = "com.adebojel.dynamicislandultra.MEDIA_SESSION_CHANGED"
    private var appContext: Context? = null
    private var controller: MediaController? = null
    private var observer: MediaController.Callback? = null

    @Synchronized
    fun attach(context: Context, token: MediaSession.Token) {
        val next = runCatching { MediaController(context, token) }.getOrNull() ?: return
        if (controller?.sessionToken == next.sessionToken) {
            publish()
            return
        }
        detach()
        appContext = context.applicationContext
        controller = next
        val callback = object : MediaController.Callback() {
            override fun onMetadataChanged(metadata: MediaMetadata?) = publish()
            override fun onPlaybackStateChanged(state: PlaybackState?) = publish()
            override fun onSessionDestroyed() {
                detach()
                publish()
            }
        }
        observer = callback
        next.registerCallback(callback)
        publish()
    }

    @Synchronized
    fun clearIfPackageMatches(packageName: String) {
        if (controller?.packageName == packageName &&
            controller?.playbackState?.state != PlaybackState.STATE_PLAYING
        ) {
            detach()
            publish()
        }
    }

    @Synchronized
    fun snapshot(): MediaSessionSnapshot? {
        val active = controller ?: return null
        val metadata = active.metadata
        val playback = active.playbackState
        val duration = metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION)?.coerceAtLeast(0L) ?: 0L
        val basePosition = playback?.position?.coerceAtLeast(0L) ?: 0L
        val position = if (playback?.state == PlaybackState.STATE_PLAYING && playback.lastPositionUpdateTime > 0L) {
            (basePosition + ((SystemClock.elapsedRealtime() - playback.lastPositionUpdateTime) * playback.playbackSpeed).toLong())
                .coerceAtMost(duration.takeIf { it > 0L } ?: Long.MAX_VALUE)
        } else basePosition
        return MediaSessionSnapshot(
            packageName = active.packageName,
            title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE).orEmpty(),
            artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST).orEmpty(),
            album = metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM).orEmpty(),
            playing = playback?.state == PlaybackState.STATE_PLAYING,
            positionMs = position,
            durationMs = duration,
            artwork = metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART)
                ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
        )
    }

    fun togglePlayback() {
        val active = controller ?: return
        if (active.playbackState?.state == PlaybackState.STATE_PLAYING) active.transportControls.pause()
        else active.transportControls.play()
    }

    fun skipNext() { controller?.transportControls?.skipToNext() }
    fun skipPrevious() { controller?.transportControls?.skipToPrevious() }
    fun seekTo(positionMs: Long) { controller?.transportControls?.seekTo(positionMs.coerceAtLeast(0L)) }

    @Synchronized
    private fun publish() {
        val context = appContext ?: return
        val state = snapshot()
        context.sendBroadcast(Intent(ACTION_CHANGED).setPackage(context.packageName).apply {
            putExtra("title", state?.title.orEmpty())
            putExtra("artist", state?.artist.orEmpty())
            putExtra("playing", state?.playing == true)
            putExtra("package", state?.packageName.orEmpty())
        })
    }

    private fun detach() {
        val current = controller
        val callback = observer
        if (current != null && callback != null) runCatching { current.unregisterCallback(callback) }
        controller = null
        observer = null
    }

    @Synchronized
    fun release() {
        detach()
        appContext = null
    }
}
