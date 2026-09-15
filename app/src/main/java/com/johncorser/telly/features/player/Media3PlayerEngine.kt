package com.johncorser.telly.features.player

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Media3-backed [PlayerEngine]. HLS, progressive and raw TS streams are all
 * handled by ExoPlayer's default media source factory (media3-exoplayer-hls
 * is on the classpath); audio focus comes from the audio attributes below.
 * The player instance is reused across channel changes, so the last frame
 * stays on the surface while the next stream tunes (round3 P0 item 2).
 */
class Media3PlayerEngine(
    val player: ExoPlayer,
) : PlayerEngine,
    Player.Listener {
    private val mutableState = MutableStateFlow<PlayerState>(PlayerState.Idle)
    private val mutableVideo = MutableStateFlow<VideoDetails?>(null)
    private var frameRates = FrameRateEstimator()

    override val state: StateFlow<PlayerState> = mutableState.asStateFlow()
    override val video: StateFlow<VideoDetails?> = mutableVideo.asStateFlow()

    init {
        player.addListener(this)
        player.setVideoFrameMetadataListener { presentationTimeUs, _, _, _ ->
            frameRates.onFrame(presentationTimeUs)?.let(::onFrameRateMeasured)
        }
    }

    override fun load(streamUrl: String) {
        mutableState.value = PlayerState.Buffering
        frameRates = FrameRateEstimator()
        player.setMediaItem(MediaItem.fromUri(streamUrl))
        player.prepare()
        player.play()
    }

    override fun stop() {
        player.stop()
        mutableState.value = PlayerState.Idle
    }

    override fun release() {
        player.release()
    }

    override fun setMuted(muted: Boolean) {
        player.volume = if (muted) 0f else 1f
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            Player.STATE_BUFFERING -> mutableState.value = PlayerState.Buffering
            Player.STATE_READY -> onReady()
            else -> Unit
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        mutableState.value = PlayerState.Error(error.errorCodeName)
    }

    private fun onReady() {
        mutableState.value = PlayerState.Playing
        val videoFormat = player.videoFormat
        mutableVideo.value =
            VideoDetails(
                width = videoFormat?.width ?: 0,
                height = videoFormat?.height ?: 0,
                frameRate = (videoFormat?.frameRate ?: 0f).coerceAtLeast(0f),
                audioChannels = player.audioFormat?.channelCount ?: 0,
            )
    }

    /** TS formats report no frame rate; the render-time estimate fills it in. */
    private fun onFrameRateMeasured(fps: Float) {
        mutableVideo.update { details ->
            if (details != null && details.frameRate <= 0f) details.copy(frameRate = fps) else details
        }
    }

    companion object {
        /**
         * Built here so devices get audio focus and TV-ready defaults.
         * Multiview panes pass [handleAudioFocus] = false: N players each
         * grabbing focus would pause one another, so the pool's engines
         * share the app's focus and the focused pane owns audio by mute
         * state instead. Single fullscreen playback keeps the default.
         */
        fun create(
            context: Context,
            handleAudioFocus: Boolean = true,
        ): Media3PlayerEngine {
            val audioAttributes =
                AudioAttributes
                    .Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
                    .build()
            val player =
                ExoPlayer
                    .Builder(context)
                    .setMediaSourceFactory(streamMediaSourceFactory(context))
                    .setAudioAttributes(audioAttributes, handleAudioFocus)
                    .build()
            return Media3PlayerEngine(player)
        }
    }
}
