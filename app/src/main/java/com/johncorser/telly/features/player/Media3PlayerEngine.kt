package com.johncorser.telly.features.player

import android.content.Context
import android.os.Handler
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.johncorser.telly.features.player.tracks.ExoTrackFacade
import com.johncorser.telly.features.player.tracks.TrackFacade
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Media3-backed [PlayerEngine]. HLS, progressive and raw TS streams are all
 * handled by ExoPlayer's default media source factory (media3-exoplayer-hls
 * is on the classpath); audio focus comes from the audio attributes below.
 * The player instance is reused across channel changes, so the last frame
 * stays on the surface while the next stream tunes (round3 P0 item 2).
 */
class Media3PlayerEngine(
    val player: ExoPlayer,
    private val userAgent: StreamUserAgent? = null,
    override val tracks: TrackFacade = ExoTrackFacade(player),
    override val decoders: DecoderPreferences = DecoderPreferences.NONE,
    private val reconnect: ReconnectPolicy = ReconnectPolicy(),
    // Retries post on the player's own (main) thread; tests inject a capturing
    // scheduler so the reconnect logic runs without a real Handler.
    private val schedule: (Long, () -> Unit) -> Unit = { d, t -> Handler(player.applicationLooper).postDelayed(t, d) },
) : PlayerEngine,
    Player.Listener {
    private val mutableState = MutableStateFlow<PlayerState>(PlayerState.Idle)
    private val videoFeed = EngineVideoFeed()
    private val mutablePaused = MutableStateFlow(false)
    private val reconnector = EngineReconnector(player, reconnect, schedule)

    override val state: StateFlow<PlayerState> = mutableState.asStateFlow()
    override val video: StateFlow<VideoDetails?> = videoFeed.video
    override val paused: StateFlow<Boolean> = mutablePaused.asStateFlow()

    init {
        player.addListener(this)
        player.setVideoFrameMetadataListener { presentationTimeUs, _, _, _ ->
            videoFeed.onFrame(presentationTimeUs)
        }
    }

    /** Skips a re-load of the same stream (guide <-> fullscreen hand-over). */
    private val active = ActiveStream()

    override fun load(streamUrl: String) {
        if (active.isCurrent(streamUrl, mutableState.value, mutablePaused.value)) return
        active.onLoad(streamUrl)
        mutableState.value = PlayerState.Buffering
        mutablePaused.value = false
        reconnector.reset()
        videoFeed.reset()
        userAgent?.onLoad(streamUrl)
        player.setMediaItem(MediaItem.fromUri(streamUrl))
        player.prepare()
        player.play()
    }

    override fun stop() {
        active.onStop()
        player.stop()
        reconnector.reset()
        mutableState.value = PlayerState.Idle
        mutablePaused.value = false
    }

    override fun release() {
        player.release()
    }

    override fun setMuted(muted: Boolean) {
        player.volume = if (muted) 0f else 1f
    }

    override fun pause() {
        player.pause()
        mutablePaused.value = true
    }

    override fun resume() {
        player.play()
        mutablePaused.value = false
    }

    override fun positionMs(): Long = player.currentPosition

    override fun seekTo(positionMs: Long) = player.seekTo(positionMs)

    override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            Player.STATE_BUFFERING -> mutableState.value = PlayerState.Buffering
            Player.STATE_READY -> {
                reconnector.reset()
                mutableState.value = PlayerState.Playing
                videoFeed.onReady(player)
            }
            Player.STATE_ENDED -> mutableState.value = PlayerState.Ended
            else -> Unit
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        mutableState.value = reconnector.onError(error)
    }

    companion object {
        /**
         * Built in [buildMedia3PlayerEngine] so devices get audio focus and
         * TV-ready defaults. Multiview panes pass [handleAudioFocus] = false:
         * N players each grabbing focus would pause one another, so the
         * pool's engines share the app's focus and the focused pane owns
         * audio by mute state instead. Single fullscreen keeps the default.
         * [audio] carries the persisted surround-by-default + passthrough
         * choices (defaults off = the previous behavior); [tuning] the
         * Playback buffer-size + decoder rows read at build time.
         */
        fun create(
            context: Context,
            handleAudioFocus: Boolean = true,
            userAgentFor: (streamUrl: String) -> String = { STREAM_USER_AGENT },
            audio: PlayerAudioPrefs = PlayerAudioPrefs(),
            tuning: PlayerTuning = PlayerTuning(),
        ): Media3PlayerEngine = buildMedia3PlayerEngine(context, handleAudioFocus, userAgentFor, audio, tuning)
    }
}
